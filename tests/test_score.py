import unittest
from collections import Counter
from unittest.mock import patch

from sparkler.control.score import KeywordScorer, apply_scores, score_text, train
from sparkler.control import store


class ScoreTests(unittest.TestCase):
    def test_prefers_relevant_words(self):
        pos = Counter(_tok("glacier climate polar ice"))
        neg = Counter(_tok("sports football score"))
        s = KeywordScorer(pos, neg, 1, 1)
        hi = score_text(s, "polar glacier research")
        lo = score_text(s, "football sports night")
        self.assertGreater(hi, lo)
        long_hi = score_text(s, "polar glacier research " * 400)
        self.assertLess(abs(long_hi - hi), 0.01)

    def test_none_scorer(self):
        self.assertEqual(score_text(None, "anything"), 0.0)

    def test_train_uses_url_when_body_missing(self):
        labs = {
            "https://news.example/polar-ice": "highly",
            "https://sports.example/football": "not",
        }
        with patch.object(store, "labels", return_value=labs):
            out = train("job", lambda u: "")
        self.assertTrue(out["ok"])
        self.assertEqual(out["relevant"], 1)
        self.assertEqual(out["not"], 1)
        s = out["scorer"]
        self.assertGreater(s.score("https://news.example/polar-ice"), s.score("https://sports.example/football"))

    def test_apply_scores_skips_without_model(self):
        self.assertEqual(apply_scores(None, "job", None), 0)

    def test_apply_scores_atomic_set(self):
        class FakeDB:
            def __init__(self):
                self.calls = []
                self.updates = []

            def docs(self, **kw):
                self.calls.append(kw)
                extra = kw.get("q", "")
                if "UNFETCHED" in extra or kw.get("start"):
                    return []
                return [
                    {"id": "a", "url": "https://ice.example/", "extracted_text": "polar glacier ice"},
                    {"id": "b", "url": "https://ball.example/", "extracted_text": "football sports"},
                ]

            def set_fields(self, updates, commit=True):
                self.updates.extend(updates)

        pos = Counter(_tok("glacier climate polar ice"))
        neg = Counter(_tok("sports football score"))
        db = FakeDB()
        n = apply_scores(db, "job", KeywordScorer(pos, neg, 1, 1))
        self.assertEqual(n, 2)
        by_id = {u["id"]: u["page_score"] for u in db.updates}
        self.assertGreater(by_id["a"], by_id["b"])


def _tok(s):
    from sparkler.control.score import _tokens
    return _tokens(s)


if __name__ == "__main__":
    unittest.main()
