"""Solr CrawlDB client."""
from __future__ import annotations

from datetime import datetime, timezone

import httpx

from . import config


def _now():
    return datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")


class CrawlDB:
    def __init__(self, base=None):
        self.base = (base or config.solr_base()).rstrip("/")
        self._c = httpx.Client(timeout=30.0)

    def close(self):
        self._c.close()

    def ping(self) -> bool:
        try:
            r = self._c.get(f"{self.base}/admin/ping")
            return r.status_code == 200
        except Exception:
            return False

    def add(self, docs, commit=True):
        if not isinstance(docs, list):
            docs = [docs]
        params = {"commit": "true"} if commit else {"softCommit": "true"}
        r = self._c.post(f"{self.base}/update", params=params, json=docs)
        r.raise_for_status()
        return r.json() if r.content else {}

    def delete_job(self, crawl_id: str):
        q = f'crawl_id:"{_esc(crawl_id)}"'
        r = self._c.post(
            f"{self.base}/update",
            params={"commit": "true"},
            json={"delete": {"query": q}},
        )
        r.raise_for_status()

    def delete_all(self):
        r = self._c.post(
            f"{self.base}/update",
            params={"commit": "true"},
            json={"delete": {"query": "*:*"}},
        )
        r.raise_for_status()

    def select(self, q="*:*", rows=10, start=0, sort=None, fl=None, fq=None, facet_fields=None):
        params = {
            "q": q,
            "rows": rows,
            "start": start,
            "wt": "json",
        }
        if sort:
            params["sort"] = sort
        if fl:
            params["fl"] = fl
        if fq:
            params["fq"] = fq
        if facet_fields:
            params["facet"] = "true"
            params["facet.field"] = facet_fields
            params["facet.mincount"] = 1
            params["facet.limit"] = 200
        r = self._c.get(f"{self.base}/select", params=params)
        r.raise_for_status()
        return r.json()

    def docs(self, **kw):
        data = self.select(**kw)
        return data.get("response", {}).get("docs", [])

    def count(self, q="*:*", fq=None):
        data = self.select(q=q, rows=0, fq=fq)
        return data.get("response", {}).get("numFound", 0)

    def facets(self, q="*:*", fields=("status", "hostname", "label", "discover_depth")):
        data = self.select(q=q, rows=0, facet_fields=list(fields))
        raw = data.get("facet_counts", {}).get("facet_fields", {})
        out = {}
        for f, pairs in raw.items():
            items = []
            it = iter(pairs)
            for k in it:
                v = next(it, 0)
                items.append({"value": str(k), "count": v})
            out[f] = items
        out["numFound"] = data.get("response", {}).get("numFound", 0)
        return out

    def get(self, doc_id: str):
        found = self.docs(q=f'id:"{_esc(doc_id)}"', rows=1)
        return found[0] if found else None


def _esc(s: str) -> str:
    return s.replace("\\", "\\\\").replace('"', '\\"')


def job_query(crawl_id: str, extra="") -> str:
    q = f'crawl_id:"{_esc(crawl_id)}"'
    if extra:
        q = f"({q}) AND ({extra})"
    return q


def stamp(doc: dict) -> dict:
    doc = dict(doc)
    doc.setdefault("indexed_at", _now())
    doc.setdefault("crawler", "sparkler")
    return doc
