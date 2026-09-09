"""Tika extract + HTML outlink harvest."""
from __future__ import annotations

import json
import re
from html.parser import HTMLParser

from .urls import normalize

_MD_KEY = re.compile(r"[^A-Za-z0-9]+")

_TEXT_CAP = 400_000


class _LinkParser(HTMLParser):
    def __init__(self):
        super().__init__(convert_charrefs=True)
        self.hrefs = []
        self.title = ""
        self._in_title = False
        self.text_bits = []

    def handle_starttag(self, tag, attrs):
        d = {k.lower(): v for k, v in attrs}
        if tag == "a" and d.get("href"):
            self.hrefs.append(d["href"])
        if tag == "title":
            self._in_title = True

    def handle_endtag(self, tag):
        if tag == "title":
            self._in_title = False

    def handle_data(self, data):
        if self._in_title:
            self.title += data
        self.text_bits.append(data)


def html_links(html: str, base: str) -> tuple[str, list[str], str]:
    p = _LinkParser()
    try:
        p.feed(html)
        p.close()
    except Exception:
        pass
    title = " ".join(p.title.split())[:300]
    text = " ".join("".join(p.text_bits).split())
    links = []
    seen = set()
    for href in p.hrefs:
        n = normalize(href, base=base)
        if n and n not in seen:
            seen.add(n)
            links.append(n)
    return title, links, text


def _merge_links(*lists):
    seen = set()
    out = []
    for lst in lists:
        for u in lst or []:
            if u and u not in seen:
                seen.add(u)
                out.append(u)
    return out


def parse(url: str, content: bytes, content_type: str = "") -> dict:
    html = ""
    try:
        html = content.decode("utf-8", errors="replace")
    except Exception:
        html = ""
    mime_hint = (content_type or "").lower()
    looks_html = "html" in mime_hint or html.lstrip()[:32].lower().startswith(("<!", "<html", "<head"))
    title, html_hrefs, fallback_text = html_links(html, url) if looks_html and html else ("", [], "")
    text = fallback_text
    meta_title = title
    mime = content_type or "application/octet-stream"
    metadata = {}
    tika_hrefs = []
    try:
        from tika import parser as tika_parser
        parsed = tika_parser.from_buffer(content, xmlContent=True)
        if parsed:
            xhtml = (parsed.get("content") or "").strip()
            if xhtml:
                tika_title, tika_hrefs, tika_text = html_links(xhtml, url)
                if tika_text:
                    text = tika_text
                if tika_title:
                    meta_title = tika_title
            md = parsed.get("metadata") or {}
            if isinstance(md, dict):
                metadata = clean_metadata(md)
                mime = _first(metadata.get("Content-Type")) or mime
                meta_title = _first(metadata.get("title") or metadata.get("dc:title")) or meta_title
    except Exception:
        pass
    text = " ".join(text.split())[:_TEXT_CAP]
    if not meta_title:
        meta_title = url
    return {
        "title": meta_title[:300],
        "text": text,
        "outlinks": _merge_links(html_hrefs, tika_hrefs),
        "content_type": (mime or "")[:120],
        "metadata": metadata,
        "tika_metadata": json.dumps(metadata, ensure_ascii=False),
        "solr_md": solr_md_fields(metadata),
    }


def clean_metadata(md: dict) -> dict:
    out = {}
    for k, v in (md or {}).items():
        if v is None:
            continue
        key = str(k)
        if isinstance(v, list):
            vals = [str(x) for x in v if x is not None and str(x).strip()]
            if not vals:
                continue
            out[key] = vals if len(vals) > 1 else vals[0]
        else:
            s = str(v)
            if s.strip():
                out[key] = s
    return out


def solr_md_fields(meta: dict) -> dict:
    fields = {}
    for k, v in (meta or {}).items():
        name = md_field(k)
        if isinstance(v, list):
            fields[name] = [str(x)[:800] for x in v[:30]]
        else:
            fields[name] = [str(v)[:800]]
    return fields


def md_field(key: str) -> str:
    s = _MD_KEY.sub("_", key).strip("_")
    if not s:
        s = "tika"
    if s[0].isdigit():
        s = "tika_" + s
    return s[:80] + "_s_md"


def _first(val):
    if val is None:
        return None
    if isinstance(val, list):
        return str(val[0]) if val else None
    return str(val)
