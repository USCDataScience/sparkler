"""Tika extract + HTML outlink harvest."""
from __future__ import annotations

from html.parser import HTMLParser
from urllib.parse import urljoin

from .urls import normalize

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


def parse(url: str, content: bytes, content_type: str = "") -> dict:
    html = ""
    try:
        html = content.decode("utf-8", errors="replace")
    except Exception:
        html = ""
    title, links, fallback_text = html_links(html, url) if html else ("", [], "")
    text = fallback_text
    meta_title = title
    mime = content_type or "application/octet-stream"
    try:
        from tika import parser as tika_parser
        parsed = tika_parser.from_buffer(content, xmlContent=False)
        if parsed:
            tika_text = (parsed.get("content") or "").strip()
            if tika_text:
                text = tika_text
            md = parsed.get("metadata") or {}
            if isinstance(md, dict):
                mime = _first(md.get("Content-Type") or md.get("Content-Type")) or mime
                meta_title = _first(md.get("title") or md.get("dc:title")) or title
                # Tika sometimes lists links
    except Exception:
        pass
    text = " ".join(text.split())[:_TEXT_CAP]
    if not meta_title:
        meta_title = url
    return {
        "title": meta_title[:300],
        "text": text,
        "outlinks": links,
        "content_type": (mime or "")[:120],
    }


def _first(val):
    if val is None:
        return None
    if isinstance(val, list):
        return str(val[0]) if val else None
    return str(val)
