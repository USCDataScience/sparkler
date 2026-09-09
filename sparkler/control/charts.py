"""Crawl-time heatmap and Tika metadata rollups from FETCHED Solr docs."""
from __future__ import annotations

import json
from collections import Counter
from datetime import datetime, timedelta, timezone


def parse_ts(raw) -> datetime | None:
    if not raw:
        return None
    if isinstance(raw, list):
        raw = raw[0] if raw else None
    if raw is None:
        return None
    s = str(raw).strip()
    if not s:
        return None
    if s.endswith("Z"):
        s = s[:-1] + "+00:00"
    try:
        dt = datetime.fromisoformat(s)
    except ValueError:
        return None
    if dt.tzinfo is None:
        dt = dt.replace(tzinfo=timezone.utc)
    return dt.astimezone(timezone.utc)


def meta_obj(raw) -> dict:
    if not raw:
        return {}
    if isinstance(raw, dict):
        return raw
    if isinstance(raw, list):
        raw = raw[0] if raw else ""
    try:
        data = json.loads(raw)
        return data if isinstance(data, dict) else {}
    except Exception:
        return {}


def _first(val) -> str:
    if val is None:
        return ""
    if isinstance(val, list):
        return str(val[0]) if val else ""
    return str(val)


def bucket_seconds(span_s: float) -> int:
    if span_s <= 120:
        return 5
    if span_s <= 600:
        return 10
    if span_s <= 3600:
        return 60
    if span_s <= 86400:
        return 300
    return 3600


def time_label(ts: datetime, step: int) -> str:
    if step < 60:
        return ts.strftime("%H:%M:%S")
    if step < 3600:
        return ts.strftime("%H:%M")
    if step < 86400:
        return ts.strftime("%m-%d %H:%M")
    return ts.strftime("%Y-%m-%d")


_DEPTH_ORDER = ["0", "1", "2", "3", "4–7", "8–15", "16+"]


def depth_bin(raw) -> str:
    try:
        n = int(raw or 0)
    except (TypeError, ValueError):
        n = 0
    if n <= 3:
        return str(n)
    if n <= 7:
        return "4–7"
    if n <= 15:
        return "8–15"
    return "16+"


def heatmap(docs: list[dict]) -> dict:
    rows = []
    for d in docs:
        ts = parse_ts(d.get("fetch_timestamp"))
        if not ts:
            continue
        rows.append((ts, depth_bin(d.get("discover_depth"))))
    if not rows:
        return {"x": [], "y": [], "cells": [], "series": [], "step_s": 0, "span_s": 0}
    t0 = min(t for t, _ in rows)
    t1 = max(t for t, _ in rows)
    span = max(1.0, (t1 - t0).total_seconds())
    step = bucket_seconds(span)
    counts = Counter()
    by_slot = Counter()
    for ts, depth in rows:
        slot = int((ts - t0).total_seconds() // step)
        counts[(slot, depth)] += 1
        by_slot[slot] += 1
    max_slot = max(by_slot)
    x_labels = [time_label(t0 + timedelta(seconds=i * step), step) for i in range(max_slot + 1)]
    present = {d for _, d in counts}
    ys = [b for b in _DEPTH_ORDER if b in present]
    cells = [{"x": x_labels[s], "y": d, "n": n} for (s, d), n in counts.items() if s < len(x_labels)]
    series = [{"t": x_labels[i], "n": by_slot[i]} for i in range(len(x_labels))]
    return {
        "x": x_labels,
        "y": ys,
        "cells": cells,
        "series": series,
        "step_s": step,
        "span_s": span,
        "start": t0.strftime("%Y-%m-%dT%H:%M:%SZ"),
        "end": t1.strftime("%Y-%m-%dT%H:%M:%SZ"),
        "fetched_with_time": len(rows),
    }


def tika_rollups(docs: list[dict]) -> dict:
    types = Counter()
    langs = Counter()
    encodings = Counter()
    keys = Counter()
    parsers = Counter()
    parse_ms = []
    responses = []
    depths = Counter()
    mimes = Counter()
    for d in docs:
        mime = d.get("content_type") or ""
        if isinstance(mime, list):
            mime = mime[0] if mime else ""
        if mime:
            mimes[str(mime).split(";")[0].strip()] += 1
        dep = int(d.get("discover_depth") or 0)
        depths[str(dep) if dep <= 10 else "11+"] += 1
        rt = d.get("response_time")
        if rt not in (None, ""):
            try:
                responses.append(int(rt))
            except (TypeError, ValueError):
                pass
        md = meta_obj(d.get("tika_metadata"))
        for k in md:
            keys[k] += 1
        ct = _first(md.get("Content-Type")) or (str(mime) if mime else "")
        if ct:
            types[ct.split(";")[0].strip()] += 1
        lang = _first(md.get("Content-Language") or md.get("language") or md.get("dc:language"))
        if lang:
            langs[lang] += 1
        enc = _first(md.get("Content-Encoding") or md.get("X-TIKA:detectedEncoding"))
        if enc:
            encodings[enc] += 1
        parsed = md.get("X-TIKA:Parsed-By") or []
        if isinstance(parsed, str):
            parsed = [parsed]
        for p in parsed:
            short = str(p).rsplit(".", 1)[-1]
            parsers[short] += 1
        ms = _first(md.get("X-TIKA:parse_time_millis"))
        if ms:
            try:
                parse_ms.append(float(ms))
            except ValueError:
                pass
    def top(counter, n=12):
        return [{"value": k, "count": v} for k, v in counter.most_common(n)]

    def nums(vals):
        if not vals:
            return {"n": 0, "avg": None, "max": None, "p50": None}
        s = sorted(vals)
        mid = s[len(s) // 2]
        return {
            "n": len(s),
            "avg": round(sum(s) / len(s), 1),
            "max": s[-1],
            "p50": mid,
        }

    def hist(vals, bins=10):
        if not vals:
            return []
        lo, hi = min(vals), max(vals)
        if lo == hi:
            return [{"value": str(int(lo)), "count": len(vals)}]
        width = (hi - lo) / bins
        buckets = [0] * bins
        for v in vals:
            i = min(bins - 1, int((v - lo) / width))
            buckets[i] += 1
        out = []
        for i, n in enumerate(buckets):
            a = int(lo + i * width)
            b = int(lo + (i + 1) * width)
            out.append({"value": f"{a}–{b}", "count": n})
        return out

    return {
        "mime": top(mimes),
        "tika_type": top(types),
        "language": top(langs),
        "encoding": top(encodings),
        "tika_keys": top(keys, 20),
        "parsers": top(parsers),
        "depth": [{"value": k, "count": depths[k]} for k in sorted(depths, key=lambda x: int(x.replace("+", "")))],
        "parse_ms": nums(parse_ms),
        "response_ms": nums(responses),
        "response_hist": hist(responses),
        "docs": len(docs),
        "docs_with_tika": sum(1 for d in docs if meta_obj(d.get("tika_metadata"))),
    }


def from_solr_docs(docs: list[dict]) -> dict:
    return {
        "heatmap": heatmap(docs),
        "tika": tika_rollups(docs),
    }
