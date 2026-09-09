"""Control API + View static files."""
from __future__ import annotations

import json
import threading
from typing import Optional

from fastapi import FastAPI, HTTPException, Query
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import Response
from fastapi.staticfiles import StaticFiles
from pydantic import BaseModel

from . import config
from . import solr_server
from .control import store
from .control import score as scorer
from .control import charts as chartlib
from .crawl.loop import crawl as run_crawl
from .crawl.loop import inject
from .crawl.urls import doc_id, normalize
from .paths import WEB_DIST
from .solr import CrawlDB, job_query

app = FastAPI(title="Sparkler")
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

_runs: dict[str, dict] = {}
_stops: dict[str, threading.Event] = {}
_lock = threading.Lock()


class JobIn(BaseModel):
    id: str
    urls: list[str] = []


class SeedsIn(BaseModel):
    urls: list[str] = []


class LabelIn(BaseModel):
    url: str
    label: str = ""


class CrawlIn(BaseModel):
    topn: int = 50
    iterations: int = -1
    same_host: bool = True
    max_depth: int = -1
    expand: bool = True
    no_robots: bool = False


def _db() -> CrawlDB:
    solr_server.ensure()
    return CrawlDB()


@app.get("/api/health")
def health():
    return {
        "ok": True,
        "solr": solr_server.is_up(),
        "solr_url": config.solr_base(),
    }


@app.get("/api/jobs")
def jobs():
    db = _db()
    out = []
    for j in store.list_jobs():
        q = job_query(j["id"])
        fac = db.facets(q=q, fields=("status",))
        counts = {x["value"]: x["count"] for x in fac.get("status", [])}
        out.append({
            "id": j["id"],
            "created": j["created"],
            "total": fac.get("numFound", 0),
            "fetched": counts.get("FETCHED", 0),
            "unfetched": counts.get("UNFETCHED", 0),
            "errors": counts.get("ERROR", 0),
            "filtered": counts.get("FILTERED", 0),
            "seeds": len(store.seeds(j["id"])),
            "running": _runs.get(j["id"], {}).get("state") in ("running", "stopping"),
        })
    db.close()
    return {"jobs": out}


@app.post("/api/jobs")
def create_job(body: JobIn):
    job_id = body.id.strip()
    if not job_id:
        raise HTTPException(400, "id required")
    store.create_job(job_id)
    n = 0
    if body.urls:
        n = inject(job_id, body.urls, seed=True)
    return {"id": job_id, "injected": n}


@app.delete("/api/jobs/{job_id}")
def drop_job(job_id: str):
    if (_runs.get(job_id) or {}).get("state") == "running":
        raise HTTPException(409, "crawl running")
    db = _db()
    db.delete_job(job_id)
    db.close()
    store.delete_job(job_id)
    _runs.pop(job_id, None)
    _stops.pop(job_id, None)
    return {"ok": True}


@app.delete("/api/catalog")
def drop_catalog():
    if any((r or {}).get("state") == "running" for r in _runs.values()):
        raise HTTPException(409, "crawl running")
    db = _db()
    db.delete_all()
    db.close()
    store.reset_all()
    _runs.clear()
    _stops.clear()
    return {"ok": True}


@app.get("/api/jobs/{job_id}/stats")
def job_stats(job_id: str):
    db = _db()
    q = job_query(job_id)
    fac = db.facets(
        q=q,
        fields=("status", "hostname", "label", "discover_depth", "content_type"),
    )
    labs = store.labels(job_id)
    trained = scorer.train(
        job_id,
        lambda u: (db.get(doc_id(job_id, u)) or {}).get("extracted_text") or "",
    )
    db.close()
    run = _runs.get(job_id) or {}
    seeds = store.seeds(job_id)
    counts = {x["value"]: x["count"] for x in fac.get("status", [])}
    return {
        "id": job_id,
        "seeds": seeds,
        "labels": labs,
        "model": {
            "ok": trained["ok"],
            "relevant": trained["relevant"],
            "not": trained["not"],
        },
        "facets": fac,
        "total": fac.get("numFound", 0),
        "fetched": counts.get("FETCHED", 0),
        "unfetched": counts.get("UNFETCHED", 0),
        "errors": counts.get("ERROR", 0),
        "filtered": counts.get("FILTERED", 0),
        "seed_n": len(seeds),
        "hosts": len(fac.get("hostname") or []),
        "run": {k: v for k, v in run.items() if k != "thread"},
    }


@app.get("/api/jobs/{job_id}/charts")
def job_charts(job_id: str):
    db = _db()
    q = job_query(job_id, "status:FETCHED")
    data = db.select(
        q=q,
        rows=5000,
        fl="fetch_timestamp,discover_depth,content_type,response_time,tika_metadata,hostname,page_score",
    )
    db.close()
    docs = data.get("response", {}).get("docs", [])
    payload = chartlib.from_solr_docs(docs)
    payload["numFound"] = data.get("response", {}).get("numFound", 0)
    return payload


@app.get("/api/jobs/{job_id}/seeds")
def get_seeds(job_id: str, q: Optional[str] = None):
    seeds = store.seeds(job_id)
    if q:
        needle = q.lower()
        seeds = [s for s in seeds if needle in s.lower()]
    return {"seeds": seeds, "total": len(store.seeds(job_id))}


@app.post("/api/jobs/{job_id}/seeds")
def post_seeds(job_id: str, body: SeedsIn):
    n = inject(job_id, body.urls, seed=True)
    return {"injected": n, "seeds": store.seeds(job_id)}


@app.get("/api/jobs/{job_id}/documents")
def documents(
    job_id: str,
    q: Optional[str] = None,
    status: Optional[str] = None,
    hostname: Optional[str] = None,
    label: Optional[str] = None,
    content_type: Optional[str] = None,
    depth: Optional[str] = None,
    start: int = 0,
    rows: int = 25,
):
    db = _db()
    extra = []
    if status:
        extra.append(f'status:"{status}"')
    if hostname:
        key = hostname[4:] if hostname.startswith("www.") else hostname
        extra.append(f'(hostname:"{key}" OR hostname:"www.{key}")')
    if label:
        extra.append(f'label:"{label}"')
    if content_type:
        extra.append(f'(content_type:"{content_type}" OR content_type:{content_type}*)')
    if depth:
        extra.append(_depth_fq(depth))
    text_q = (q or "").strip()
    inner = f"({text_q})" if text_q else ""
    data = db.select(
        q=job_query(job_id, inner),
        fq=" AND ".join(extra) if extra else None,
        start=start,
        rows=rows,
        sort="page_score desc,discover_depth asc",
        fl="id,url,title,status,hostname,discover_depth,page_score,label,content_type,fetch_status_code,seed,parent,extracted_text,tika_metadata",
    )
    db.close()
    docs = data.get("response", {}).get("docs", [])
    labs = store.labels(job_id)
    for d in docs:
        text = d.pop("extracted_text", "") or ""
        d["snippet"] = text[:400]
        d["label"] = labs.get(d.get("url"), d.get("label") or "")
        d["metadata"] = _meta(d.pop("tika_metadata", None))
        d["metadata_n"] = len(d["metadata"])
    return {
        "documents": docs,
        "numFound": data.get("response", {}).get("numFound", 0),
        "start": start,
        "rows": rows,
    }


@app.get("/api/jobs/{job_id}/frontier")
def frontier(job_id: str, start: int = 0, rows: int = 50):
    db = _db()
    data = db.select(
        q=job_query(job_id, "status:UNFETCHED"),
        start=start,
        rows=rows,
        sort="page_score desc,discover_depth asc",
        fl="id,url,hostname,discover_depth,page_score,parent",
    )
    db.close()
    return {
        "documents": data.get("response", {}).get("docs", []),
        "numFound": data.get("response", {}).get("numFound", 0),
    }


@app.get("/api/jobs/{job_id}/page")
def page(job_id: str, url: str = Query(...)):
    db = _db()
    rec = db.get(doc_id(job_id, url))
    db.close()
    if not rec:
        raise HTTPException(404, "not found")
    rec["label"] = store.labels(job_id).get(url, rec.get("label") or "")
    rec["metadata"] = _meta(rec.get("tika_metadata"))
    return rec


def _depth_fq(depth: str) -> str:
    d = (depth or "").strip()
    ranges = {
        "4-7": "[4 TO 7]",
        "4–7": "[4 TO 7]",
        "8-15": "[8 TO 15]",
        "8–15": "[8 TO 15]",
        "16+": "[16 TO *]",
    }
    if d in ranges:
        return f"discover_depth:{ranges[d]}"
    return f'discover_depth:"{d}"'


def _meta(raw):
    if not raw:
        return {}
    if isinstance(raw, dict):
        return raw
    try:
        data = json.loads(raw)
        return data if isinstance(data, dict) else {}
    except Exception:
        return {}


@app.post("/api/jobs/{job_id}/label")
def post_label(job_id: str, body: LabelIn):
    url = normalize(body.url) or body.url
    store.set_label(job_id, url, body.label)
    db = _db()
    rec = db.get(doc_id(job_id, url))
    if rec:
        db.set_fields([{"id": rec["id"], "label": body.label or ""}], commit=True)
    db.close()
    return {"ok": True, "url": url, "label": body.label}


@app.post("/api/jobs/{job_id}/train")
def train(job_id: str, apply: bool = False):
    db = _db()
    result = scorer.train(job_id, lambda u: (db.get(doc_id(job_id, u)) or {}).get("extracted_text") or "")
    scored = 0
    if apply:
        scored = scorer.apply_scores(db, job_id, result.get("scorer"))
    db.close()
    return {
        "ok": result["ok"],
        "relevant": result["relevant"],
        "not": result["not"],
        "scored": scored,
    }


@app.post("/api/jobs/{job_id}/crawl")
def start_crawl(job_id: str, body: CrawlIn):
    with _lock:
        cur = _runs.get(job_id) or {}
        if cur.get("state") in ("running", "stopping"):
            raise HTTPException(409, "crawl already running")
        stop = threading.Event()
        _stops[job_id] = stop
        _runs[job_id] = {"state": "running", "fetched": 0, "url": "", "error": None}

    def work():
        try:
            def prog(p):
                with _lock:
                    st = "stopping" if stop.is_set() else "running"
                    _runs[job_id].update({
                        "state": st,
                        "url": p.get("url"),
                        "fetched": p.get("fetched"),
                        "iteration": p.get("iteration"),
                    })
            stats = run_crawl(
                job_id,
                topn=body.topn,
                iterations=body.iterations,
                same_host=body.same_host,
                max_depth=body.max_depth,
                expand=body.expand,
                respect_robots=not body.no_robots,
                on_progress=prog,
                stop_event=stop,
            )
            with _lock:
                state = "stopped" if stats.get("stopped") else "done"
                _runs[job_id] = {"state": state, **stats, "error": None}
        except Exception as e:
            msg = str(e)
            if len(msg) > 400:
                msg = msg[:400] + "…"
            with _lock:
                _runs[job_id] = {"state": "error", "error": msg}

    t = threading.Thread(target=work, daemon=True)
    t.start()
    _runs[job_id]["thread"] = True
    return {"ok": True, "state": "running"}


@app.post("/api/jobs/{job_id}/stop")
def stop_crawl(job_id: str):
    ev = _stops.get(job_id)
    if not ev:
        raise HTTPException(404, "no crawl")
    ev.set()
    with _lock:
        cur = _runs.get(job_id) or {}
        if cur.get("state") == "running":
            cur["state"] = "stopping"
            _runs[job_id] = cur
    return {"ok": True, "state": "stopping"}


@app.get("/api/jobs/{job_id}/run")
def crawl_run(job_id: str):
    run = _runs.get(job_id) or {"state": "idle"}
    return {k: v for k, v in run.items() if k != "thread"}


@app.get("/api/export")
def export(job: str, fmt: str = "json"):
    db = _db()
    docs = db.docs(
        q=job_query(job),
        rows=10000,
        fl="id,url,title,status,hostname,discover_depth,page_score,label,content_type,parent,seed",
    )
    db.close()
    payload = {"job": job, "documents": docs, "seeds": store.seeds(job), "labels": store.labels(job)}
    if fmt == "json":
        body = json.dumps(payload, indent=2)
        return Response(body, media_type="application/json",
                        headers={"Content-Disposition": f"attachment; filename=sparkler-{job}.json"})
    raise HTTPException(400, "fmt=json")


if WEB_DIST.exists():
    app.mount("/", StaticFiles(directory=WEB_DIST, html=True), name="ui")


def run(host="127.0.0.1", port=8180):
    import uvicorn
    solr_server.ensure()
    if not WEB_DIST.exists():
        print("UI not built (web/dist missing). API only.")
        print("  cd web && npm install && npm run build")
    uvicorn.run(app, host=host, port=port, log_level="info")
