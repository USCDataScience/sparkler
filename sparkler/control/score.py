"""Relevance scoring from labeled pages.

Needs both classes (relevant vs not). Highly maps to relevant.
"""
from __future__ import annotations

import math
import re
from collections import Counter

from . import store

_TOKEN = re.compile(r"[a-z0-9]{3,}")
_POS = {"relevant", "highly", "1", "2"}
_NEG = {"not", "0"}


def _tokens(text: str) -> list[str]:
    return _TOKEN.findall((text or "").lower())


class KeywordScorer:
    """Document-frequency naive Bayes. Each labeled page votes once per token.

    Mean log-odds so a long page does not blow up to ±10k just from length.
    """

    def __init__(self, pos: Counter, neg: Counter, n_pos: int, n_neg: int):
        self.pos = pos
        self.neg = neg
        self.n_pos = max(1, n_pos)
        self.n_neg = max(1, n_neg)

    def score(self, text: str) -> float:
        toks = set(_tokens(text))
        if not toks:
            return 0.0
        s = 0.0
        for t in toks:
            p = (self.pos[t] + 0.5) / (self.n_pos + 1)
            q = (self.neg[t] + 0.5) / (self.n_neg + 1)
            s += math.log(p / q)
        return float(s / len(toks))


def train(job_id: str, get_text) -> dict:
    pos, neg = Counter(), Counter()
    n_pos = n_neg = 0
    for lab, text in store.labeled_texts(job_id, get_text):
        bag = set(_tokens(text))
        if not bag:
            continue
        if lab in _POS:
            pos.update(bag)
            n_pos += 1
        elif lab in _NEG:
            neg.update(bag)
            n_neg += 1
    ok = n_pos >= 1 and n_neg >= 1
    return {
        "ok": ok,
        "relevant": n_pos,
        "not": n_neg,
        "scorer": KeywordScorer(pos, neg, n_pos, n_neg) if ok else None,
    }


def score_text(scorer: KeywordScorer | None, text: str) -> float:
    if scorer is None:
        return 0.0
    return scorer.score(text)


def apply_scores(db, crawl_id: str, model: KeywordScorer | None, batch=200,
                 statuses=("FETCHED", "UNFETCHED")) -> int:
    """Write page_score. FETCHED uses body text; UNFETCHED uses the URL. Atomic Solr set."""
    if model is None:
        return 0
    from ..solr import job_query
    n = 0
    for status in statuses:
        extra = f"status:{status}"
        fl = "id,url,extracted_text" if status == "FETCHED" else "id,url"
        start = 0
        while True:
            rows = db.docs(
                q=job_query(crawl_id, extra),
                start=start,
                rows=batch,
                sort="id asc",
                fl=fl,
            )
            if not rows:
                break
            updates = []
            for rec in rows:
                text = (rec.get("extracted_text") or "").strip() or rec.get("url") or ""
                updates.append({
                    "id": rec["id"],
                    "page_score": score_text(model, text),
                })
            db.set_fields(updates, commit=True)
            n += len(updates)
            start += len(rows)
            if len(rows) < batch:
                break
    return n

