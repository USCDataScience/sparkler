"""URL identity, grouping, and normalization."""
from __future__ import annotations

import hashlib
import re
from urllib.parse import urldefrag, urljoin, urlparse, urlunparse

_SCHEME = re.compile(r"^[a-zA-Z][a-zA-Z0-9+.-]*://")


def normalize(url: str, base: str | None = None) -> str | None:
    raw = (url or "").strip()
    if not raw or raw.startswith(("#", "javascript:", "mailto:", "data:")):
        return None
    if base:
        raw = urljoin(base, raw)
    if not _SCHEME.match(raw):
        raw = "https://" + raw
    raw, _frag = urldefrag(raw)
    p = urlparse(raw)
    if p.scheme not in ("http", "https") or not p.netloc:
        return None
    host = p.hostname.lower() if p.hostname else ""
    if not host:
        return None
    netloc = host
    if p.port and p.port not in (80, 443):
        netloc = f"{host}:{p.port}"
    path = p.path or "/"
    return urlunparse((p.scheme.lower(), netloc, path, "", p.query, ""))


def hostname(url: str) -> str:
    p = urlparse(url)
    return (p.hostname or "").lower()


def group_of(url: str) -> str:
    return hostname(url)


def doc_id(crawl_id: str, url: str) -> str:
    return hashlib.sha1(f"{crawl_id}\n{url}".encode("utf-8")).hexdigest()


def contenthash(body: bytes) -> str:
    return hashlib.sha1(body).hexdigest()
