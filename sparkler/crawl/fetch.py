"""Polite HTTP fetch + robots.txt."""
from __future__ import annotations

import time
from urllib.parse import urljoin, urlparse
from urllib.robotparser import RobotFileParser

import httpx

from ..config import crawl_cfg
from .urls import host_key


class Fetcher:
    def __init__(self, delay_ms=None, timeout_s=None, user_agent=None,
                 respect_robots=True, max_bytes=None):
        cfg = crawl_cfg()
        self.delay_s = (delay_ms if delay_ms is not None else cfg["delay_ms"]) / 1000.0
        self.timeout = timeout_s if timeout_s is not None else cfg["timeout_s"]
        self.ua = user_agent or cfg["user_agent"]
        self.respect_robots = respect_robots if respect_robots is not None else cfg["respect_robots"]
        self.max_bytes = max_bytes if max_bytes is not None else cfg["max_bytes"]
        self._last = {}
        self._robots = {}
        self._client = httpx.Client(
            follow_redirects=True,
            timeout=self.timeout,
            headers={"User-Agent": self.ua},
            max_redirects=8,
        )

    def close(self):
        self._client.close()

    def _wait(self, host: str):
        last = self._last.get(host, 0)
        wait = self.delay_s - (time.time() - last)
        if wait > 0:
            time.sleep(wait)

    def _can_fetch(self, url: str) -> bool:
        if not self.respect_robots:
            return True
        p = urlparse(url)
        origin = f"{p.scheme}://{p.netloc}"
        rp = self._robots.get(origin)
        if rp is None:
            rp = RobotFileParser()
            robots_url = urljoin(origin + "/", "robots.txt")
            try:
                r = self._client.get(robots_url)
                if r.status_code == 200:
                    rp.parse(r.text.splitlines())
                else:
                    rp.parse([])
            except Exception:
                rp.parse([])
            self._robots[origin] = rp
        try:
            return rp.can_fetch(self.ua, url)
        except Exception:
            return True

    def fetch(self, url: str) -> dict:
        host = host_key(url) or (urlparse(url).hostname or "").lower()
        self._wait(host)
        if not self._can_fetch(url):
            self._last[host] = time.time()
            return {
                "ok": False,
                "status_code": 0,
                "error": "robots",
                "url": url,
                "final_url": url,
                "content": b"",
                "content_type": "",
                "elapsed_ms": 0,
            }
        t0 = time.time()
        try:
            with self._client.stream("GET", url) as r:
                buf = bytearray()
                for chunk in r.iter_bytes():
                    buf.extend(chunk)
                    if len(buf) > self.max_bytes:
                        break
                body = bytes(buf)
                elapsed = int((time.time() - t0) * 1000)
                self._last[host] = time.time()
                ctype = r.headers.get("content-type", "")
                return {
                    "ok": r.status_code < 400,
                    "status_code": r.status_code,
                    "error": None if r.status_code < 400 else f"http {r.status_code}",
                    "url": url,
                    "final_url": str(r.url),
                    "content": body,
                    "content_type": ctype.split(";")[0].strip(),
                    "elapsed_ms": elapsed,
                }
        except Exception as e:
            self._last[host] = time.time()
            return {
                "ok": False,
                "status_code": 0,
                "error": str(e)[:300],
                "url": url,
                "final_url": url,
                "content": b"",
                "content_type": "",
                "elapsed_ms": int((time.time() - t0) * 1000),
            }
