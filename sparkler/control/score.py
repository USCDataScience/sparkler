"""Relevance scoring from labeled pages.

Needs both classes (relevant vs not). Highly maps to relevant.
"""
from __future__ import annotations

import math
import re
from collections import Counter

from . import store

_TOKEN = re.compile(r"[a-z0-9]{3,}")
_POS = {"relevant", "highly", "1", "2"}
_NEG = {"not", "0"}


def _tokens(text: str) -> list[str]:
    return _TOKEN.findall((text or "").lower())


class KeywordScorer:
    def __init__(self, pos: Counter, neg: Counter):
        self.pos = pos
        self.neg = neg
        self.pos_n = sum(pos.values()) or 1
        self.neg_n = sum(neg.values()) or 1

    def score(self, text: str) -> float:
        toks = _tokens(text)
        if not toks:
            return 0.0
        s = 0.0
        for t, n in Counter(toks).items():
            p = (self.pos[t] + 0.5) / self.pos_n
            q = (self.neg[t] + 0.5) / self.neg_n
            s += n * math.log(p / q)
        return float(s)


def train(job_id: str, get_text) -> dict:
    pos, neg = Counter(), Counter()
    n_pos = n_neg = 0
    for lab, text in store.labeled_texts(job_id, get_text):
        bag = Counter(_tokens(text))
        if lab in _POS:
            pos.update(bag)
            n_pos += 1
        elif lab in _NEG:
            neg.update(bag)
            n_neg += 1
    ok = n_pos >= 1 and n_neg >= 1
    return {
        "ok": ok,
        "relevant": n_pos,
        "not": n_neg,
        "scorer": KeywordScorer(pos, neg) if ok else None,
    }


def score_text(scorer: KeywordScorer | None, text: str) -> float:
    if scorer is None:
        return 0.0
    return scorer.score(text)
