"""URL identity, grouping, and normalization."""
from __future__ import annotations

import hashlib
import re
from urllib.parse import urldefrag, urljoin, urlparse, urlunparse

_SCHEME = re.compile(r"^[a-zA-Z][a-zA-Z0-9+.-]*://")


def _parse(url: str):
    try:
        return urlparse(url)
    except ValueError:
        return None


def normalize(url: str, base: str | None = None) -> str | None:
    raw = (url or "").strip()
    if not raw or raw.startswith(("#", "javascript:", "mailto:", "data:", "tel:", "sms:")):
        return None
    if base:
        raw = urljoin(base, raw)
    if not _SCHEME.match(raw):
        raw = "https://" + raw
    raw, _frag = urldefrag(raw)
    p = _parse(raw)
    if p is None or p.scheme not in ("http", "https") or not p.netloc:
        return None
    try:
        host = p.hostname.lower() if p.hostname else ""
        port = p.port
    except ValueError:
        return None
    if not host:
        return None
    netloc = host
    if port and port not in (80, 443):
        netloc = f"{host}:{port}"
    path = p.path or "/"
    return urlunparse((p.scheme.lower(), netloc, path, "", p.query, ""))


def hostname(url: str) -> str:
    p = _parse(url)
    if p is None:
        return ""
    try:
        return (p.hostname or "").lower()
    except ValueError:
        return ""


def host_key(url: str) -> str:
    """Apex host: www.mattmann.ai and mattmann.ai are the same site."""
    h = hostname(url)
    if h.startswith("www."):
        return h[4:]
    return h


def host_variants(key: str) -> list[str]:
    key = (key or "").lower()
    if key.startswith("www."):
        key = key[4:]
    if not key:
        return []
    return [key, "www." + key]


def group_of(url: str) -> str:
    return host_key(url) or hostname(url)


def doc_id(crawl_id: str, url: str) -> str:
    return hashlib.sha1(f"{crawl_id}\n{url}".encode("utf-8")).hexdigest()


def contenthash(body: bytes) -> str:
    return hashlib.sha1(body).hexdigest()
