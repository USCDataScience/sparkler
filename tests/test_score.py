import unittest
from sparkler.control.score import KeywordScorer, score_text
from collections import Counter


class ScoreTests(unittest.TestCase):
    def test_prefers_relevant_words(self):
        pos = Counter(_tok("glacier climate polar ice"))
        neg = Counter(_tok("sports football score"))
        s = KeywordScorer(pos, neg)
        hi = score_text(s, "polar glacier research")
        lo = score_text(s, "football sports night")
        self.assertGreater(hi, lo)

    def test_none_scorer(self):
        self.assertEqual(score_text(None, "anything"), 0.0)


def _tok(s):
    from sparkler.control.score import _tokens
    return _tokens(s)


if __name__ == "__main__":
    unittest.main()
