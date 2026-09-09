import inspect
import unittest

from sparkler.crawl.filters import URLFilter
from sparkler.crawl.loop import crawl, reopen_allowed


class ReopenTests(unittest.TestCase):
    def test_same_host_off_restores_offsite(self):
        db = FakeDB([
            {"id": "a", "url": "https://irds.usc.edu/x", "parent": "", "discover_depth": 0},
            {"id": "b", "url": "https://nasa.gov/y", "parent": "https://irds.usc.edu/", "discover_depth": 1},
            {"id": "c", "url": "https://example.com/a.png", "parent": "", "discover_depth": 1},
        ])
        f = URLFilter(same_host=False, seed_hosts=["irds.usc.edu"])
        n = reopen_allowed(db, "job", f, batch=10)
        self.assertEqual(n, 2)
        self.assertEqual({u["id"] for u in db.updates}, {"a", "b"})
        self.assertTrue(all(u["status"] == "UNFETCHED" for u in db.updates))

    def test_same_host_on_keeps_offsite_filtered(self):
        db = FakeDB([
            {"id": "a", "url": "https://irds.usc.edu/x", "parent": "", "discover_depth": 0},
            {"id": "b", "url": "https://nasa.gov/y", "parent": "https://irds.usc.edu/", "discover_depth": 1},
        ])
        f = URLFilter(same_host=True, seed_hosts=["irds.usc.edu"])
        n = reopen_allowed(db, "job", f)
        self.assertEqual(n, 1)
        self.assertEqual(db.updates[0]["id"], "a")

    def test_crawl_defaults_to_expand(self):
        params = inspect.signature(crawl).parameters
        self.assertTrue(params["expand"].default)

    def test_respects_max_depth(self):
        db = FakeDB([
            {"id": "a", "url": "https://nasa.gov/y", "parent": "", "discover_depth": 4},
        ])
        f = URLFilter(same_host=False, seed_hosts=["irds.usc.edu"])
        n = reopen_allowed(db, "job", f, max_depth=2)
        self.assertEqual(n, 0)


class FakeDB:
    def __init__(self, rows):
        self._rows = rows
        self.updates = []

    def docs(self, **kw):
        if kw.get("start"):
            return []
        return list(self._rows)

    def set_fields(self, updates, commit=True):
        self.updates.extend(updates)


if __name__ == "__main__":
    unittest.main()
