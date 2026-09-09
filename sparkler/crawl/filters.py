"""Nutch-style regex URL filter and same-host rule."""
from __future__ import annotations

import re
from urllib.parse import urlparse

from ..paths import FILTER_PATH
from .urls import hostname


def load_rules(path=None):
    p = path or FILTER_PATH
    rules = []
    if not p.exists():
        return [("+", re.compile("."))]
    for line in p.read_text().splitlines():
        line = line.strip()
        if not line or line.startswith("#"):
            continue
        sign, pattern = line[0], line[1:]
        if sign not in "+-":
            continue
        rules.append((sign, re.compile(pattern, re.I)))
    return rules or [("+", re.compile("."))]


class URLFilter:
    def __init__(self, path=None, same_host=False, seed_hosts=None):
        self.rules = load_rules(path)
        self.same_host = same_host
        self.seed_hosts = {h.lower() for h in (seed_hosts or []) if h}

    def allow(self, url: str, parent: str | None = None) -> bool:
        if not url:
            return False
        for sign, rx in self.rules:
            if rx.search(url):
                if sign == "-":
                    return False
                break
        else:
            return False
        if self.same_host:
            host = hostname(url)
            if self.seed_hosts:
                if host not in self.seed_hosts:
                    return False
            elif parent:
                if host != hostname(parent):
                    return False
        return True


def same_host(url: str, other: str) -> bool:
    a, b = urlparse(url), urlparse(other)
    return (a.hostname or "").lower() == (b.hostname or "").lower()
