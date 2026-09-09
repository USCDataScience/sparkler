import unittest
from datetime import timezone

from sparkler.control.charts import heatmap, parse_ts, tika_rollups


class ChartTests(unittest.TestCase):
    def test_parse_ts(self):
        dt = parse_ts("2026-09-09T17:04:01Z")
        self.assertEqual(dt.tzinfo, timezone.utc)
        self.assertEqual(dt.minute, 4)

    def test_heatmap_depth_by_time(self):
        docs = [
            {"fetch_timestamp": "2026-09-09T17:00:00Z", "discover_depth": 0},
            {"fetch_timestamp": "2026-09-09T17:00:03Z", "discover_depth": 1},
            {"fetch_timestamp": "2026-09-09T17:00:08Z", "discover_depth": 12},
        ]
        h = heatmap(docs)
        self.assertTrue(h["x"])
        self.assertIn("0", h["y"])
        self.assertIn("8–15", h["y"])
        self.assertEqual(len(h["series"]), len(h["x"]))
        self.assertGreaterEqual(sum(c["n"] for c in h["cells"]), 3)

    def test_tika_keys(self):
        docs = [
            {
                "content_type": "text/html; charset=UTF-8",
                "discover_depth": 0,
                "response_time": 120,
                "tika_metadata": '{"Content-Type":"text/html","Content-Language":"en","X-TIKA:Parsed-By":["org.apache.tika.parser.html.JSoupParser"],"X-TIKA:parse_time_millis":"4"}',
            },
            {
                "content_type": "text/html",
                "discover_depth": 1,
                "response_time": 80,
                "tika_metadata": '{"Content-Type":"text/html","dc:title":"Hi"}',
            },
        ]
        t = tika_rollups(docs)
        self.assertEqual(t["docs_with_tika"], 2)
        self.assertEqual(t["language"][0]["value"], "en")
        self.assertTrue(any(x["value"] == "Content-Type" for x in t["tika_keys"]))
        self.assertEqual(t["response_ms"]["n"], 2)
        self.assertEqual(t["score_hist"], [])

    def test_score_hist(self):
        docs = [
            {"page_score": 2.0, "tika_metadata": "{}"},
            {"page_score": -1.0, "tika_metadata": "{}"},
            {"page_score": 2.0, "tika_metadata": "{}"},
        ]
        t = tika_rollups(docs)
        self.assertTrue(t["score_hist"])
        self.assertEqual(sum(x["count"] for x in t["score_hist"]), 3)


if __name__ == "__main__":
    unittest.main()
