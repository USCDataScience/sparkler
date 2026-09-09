"""inject → generate → fetch → parse → upsert. One process, polite, no Spark."""
from __future__ import annotations

from collections import defaultdict

from .. import solr_server
from ..control import score as scorer
from ..control import store
from ..solr import CrawlDB, job_query, stamp
from . import parse as parse_mod
from .fetch import Fetcher
from .filters import URLFilter
from .urls import contenthash, doc_id, group_of, hostname, normalize


def _ensure():
    solr_server.ensure()


def inject(crawl_id: str, urls: list[str], parent=None, depth=0, seed=False) -> int:
    _ensure()
    store.create_job(crawl_id)
    db = CrawlDB()
    docs = []
    kept = []
    for raw in urls:
        url = normalize(raw)
        if not url:
            continue
        kept.append(url)
        docs.append(stamp({
            "id": doc_id(crawl_id, url),
            "url": url,
            "crawl_id": crawl_id,
            "group": group_of(url),
            "hostname": hostname(url),
            "status": "UNFETCHED",
            "discover_depth": depth,
            "page_score": 0.0,
            "seed": bool(seed),
            "parent": parent or "",
            "crawler": "sparkler",
        }))
    if seed and kept:
        store.add_seeds(crawl_id, kept)
    if docs:
        # Don't clobber FETCHED pages if re-injecting seeds.
        fresh = []
        for d in docs:
            existing = db.get(d["id"])
            if existing and existing.get("status") not in (None, "UNFETCHED"):
                continue
            fresh.append(d)
        if fresh:
            db.add(fresh, commit=True)
    n = len(docs)
    db.close()
    return n


def _fair_generate(db: CrawlDB, crawl_id: str, topn: int) -> list[dict]:
    q = job_query(crawl_id, "status:UNFETCHED")
    rows = db.docs(
        q=q,
        rows=max(topn * 4, topn),
        sort="page_score desc,discover_depth asc",
        fl="id,url,hostname,discover_depth,page_score,parent,seed",
    )
    by_host = defaultdict(list)
    for r in rows:
        by_host[r.get("hostname") or group_of(r["url"])].append(r)
    out = []
    hosts = list(by_host.keys())
    while len(out) < topn and hosts:
        nxt = []
        for h in hosts:
            bucket = by_host[h]
            if bucket:
                out.append(bucket.pop(0))
                if len(out) >= topn:
                    break
                nxt.append(h)
        hosts = nxt
    return out


def _get_text(db: CrawlDB, crawl_id: str):
    def inner(url):
        rec = db.get(doc_id(crawl_id, url))
        return (rec or {}).get("extracted_text") or ""
    return inner


def crawl(crawl_id: str, topn=100, iterations=1, same_host=False,
          respect_robots=True, delay_ms=None, on_progress=None) -> dict:
    _ensure()
    store.create_job(crawl_id)
    db = CrawlDB()
    seed_urls = store.seeds(crawl_id)
    seed_hosts = {hostname(u) for u in seed_urls}
    ufilter = URLFilter(same_host=same_host, seed_hosts=seed_hosts)
    trained = scorer.train(crawl_id, _get_text(db, crawl_id))
    model = trained.get("scorer")
    fetcher = Fetcher(delay_ms=delay_ms, respect_robots=respect_robots)
    stats = {
        "job": crawl_id,
        "fetched": 0,
        "errors": 0,
        "filtered": 0,
        "injected": 0,
        "iterations": 0,
    }
    try:
        for it in range(max(1, iterations)):
            batch = _fair_generate(db, crawl_id, topn)
            if not batch:
                break
            stats["iterations"] = it + 1
            new_links = []
            updates = []
            for rec in batch:
                url = rec["url"]
                if on_progress:
                    on_progress({"url": url, "iteration": it + 1, **stats})
                if not ufilter.allow(url, parent=rec.get("parent")):
                    updates.append(stamp({
                        "id": rec["id"],
                        "url": url,
                        "crawl_id": crawl_id,
                        "group": rec.get("group") or group_of(url),
                        "hostname": hostname(url),
                        "status": "FILTERED",
                    }))
                    stats["filtered"] += 1
                    continue
                result = fetcher.fetch(url)
                if result["error"] == "robots" or not result["ok"]:
                    updates.append(stamp({
                        "id": rec["id"],
                        "url": url,
                        "crawl_id": crawl_id,
                        "group": group_of(url),
                        "hostname": hostname(url),
                        "status": "ERROR" if result["error"] != "robots" else "FILTERED",
                        "fetch_status_code": result["status_code"],
                        "response_time": result["elapsed_ms"],
                    }))
                    if result["error"] == "robots":
                        stats["filtered"] += 1
                    else:
                        stats["errors"] += 1
                    continue
                parsed = parse_mod.parse(url, result["content"], result["content_type"])
                page_score = scorer.score_text(model, parsed["text"])
                updates.append(stamp({
                    "id": rec["id"],
                    "url": url,
                    "crawl_id": crawl_id,
                    "group": group_of(url),
                    "hostname": hostname(url),
                    "status": "FETCHED",
                    "title": parsed["title"],
                    "extracted_text": parsed["text"],
                    "content_type": parsed["content_type"] or result["content_type"],
                    "outlinks": parsed["outlinks"][:500],
                    "discover_depth": rec.get("discover_depth") or 0,
                    "page_score": page_score,
                    "fetch_status_code": result["status_code"],
                    "response_time": result["elapsed_ms"],
                    "contenthash": contenthash(result["content"]),
                    "seed": rec.get("seed") is True or rec.get("seed") == True,
                    "parent": rec.get("parent") or "",
                    "fetch_timestamp": stamp({})["indexed_at"],
                }))
                stats["fetched"] += 1
                depth = int(rec.get("discover_depth") or 0) + 1
                for link in parsed["outlinks"]:
                    if not ufilter.allow(link, parent=url):
                        continue
                    new_links.append((link, url, depth))
            if updates:
                db.add(updates, commit=True)
            # inject outlinks that are new
            seen = set()
            to_add = []
            for link, parent, depth in new_links:
                if link in seen:
                    continue
                seen.add(link)
                did = doc_id(crawl_id, link)
                if db.get(did):
                    continue
                to_add.append(stamp({
                    "id": did,
                    "url": link,
                    "crawl_id": crawl_id,
                    "group": group_of(link),
                    "hostname": hostname(link),
                    "status": "UNFETCHED",
                    "discover_depth": depth,
                    "page_score": scorer.score_text(model, link),
                    "seed": False,
                    "parent": parent,
                }))
            if to_add:
                db.add(to_add, commit=True)
                stats["injected"] += len(to_add)
    finally:
        fetcher.close()
        db.close()
    return stats
