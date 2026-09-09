"""SQLite for jobs, seeds, and labels. Crawl pages live in Solr."""
from __future__ import annotations

import json
import sqlite3
import time

from ..paths import DB_PATH, ensure_data

SCHEMA = """
CREATE TABLE IF NOT EXISTS jobs (
  id TEXT PRIMARY KEY,
  created REAL NOT NULL,
  config TEXT NOT NULL DEFAULT '{}'
);
CREATE TABLE IF NOT EXISTS seeds (
  job_id TEXT NOT NULL,
  url TEXT NOT NULL,
  PRIMARY KEY (job_id, url)
);
CREATE TABLE IF NOT EXISTS labels (
  job_id TEXT NOT NULL,
  url TEXT NOT NULL,
  label TEXT NOT NULL,
  PRIMARY KEY (job_id, url)
);
"""


def connect():
    ensure_data()
    db = sqlite3.connect(DB_PATH)
    db.row_factory = sqlite3.Row
    db.executescript(SCHEMA)
    return db


def create_job(job_id: str, cfg=None):
    db = connect()
    db.execute(
        "INSERT OR IGNORE INTO jobs(id, created, config) VALUES (?,?,?)",
        (job_id, time.time(), json.dumps(cfg or {})),
    )
    db.commit()
    db.close()


def delete_job(job_id: str):
    db = connect()
    db.execute("DELETE FROM seeds WHERE job_id=?", (job_id,))
    db.execute("DELETE FROM labels WHERE job_id=?", (job_id,))
    db.execute("DELETE FROM jobs WHERE id=?", (job_id,))
    db.commit()
    db.close()


def reset_all():
    db = connect()
    db.execute("DELETE FROM seeds")
    db.execute("DELETE FROM labels")
    db.execute("DELETE FROM jobs")
    db.commit()
    db.close()


def list_jobs():
    db = connect()
    rows = [dict(r) for r in db.execute("SELECT id, created, config FROM jobs ORDER BY created DESC")]
    db.close()
    return rows


def add_seeds(job_id: str, urls: list[str]):
    create_job(job_id)
    db = connect()
    for u in urls:
        db.execute("INSERT OR IGNORE INTO seeds(job_id, url) VALUES (?,?)", (job_id, u))
    db.commit()
    db.close()


def seeds(job_id: str) -> list[str]:
    db = connect()
    rows = [r["url"] for r in db.execute("SELECT url FROM seeds WHERE job_id=?", (job_id,))]
    db.close()
    return rows


def set_label(job_id: str, url: str, label: str):
    db = connect()
    if not label:
        db.execute("DELETE FROM labels WHERE job_id=? AND url=?", (job_id, url))
    else:
        db.execute(
            "INSERT OR REPLACE INTO labels(job_id, url, label) VALUES (?,?,?)",
            (job_id, url, label),
        )
    db.commit()
    db.close()


def labels(job_id: str) -> dict[str, str]:
    db = connect()
    rows = {r["url"]: r["label"] for r in db.execute("SELECT url, label FROM labels WHERE job_id=?", (job_id,))}
    db.close()
    return rows


def labeled_texts(job_id: str, get_text) -> list[tuple[str, str]]:
    """Return (label, text) using get_text(url). URL is the bag if the page has no body."""
    out = []
    for url, lab in labels(job_id).items():
        text = (get_text(url) or "").strip() or url
        if text:
            out.append((lab, text))
    return out
