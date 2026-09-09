import tempfile
import unittest
from pathlib import Path

from sparkler.crawl.filters import URLFilter, load_rules


class FilterTests(unittest.TestCase):
    def test_default_keeps_html(self):
        f = URLFilter()
        self.assertTrue(f.allow("https://example.com/page"))

    def test_skips_images(self):
        f = URLFilter()
        self.assertFalse(f.allow("https://example.com/a.png"))
        self.assertFalse(f.allow("https://example.com/a.CSS"))

    def test_same_host(self):
        f = URLFilter(same_host=True, seed_hosts=["example.com"])
        self.assertTrue(f.allow("https://example.com/b"))
        self.assertFalse(f.allow("https://other.com/b"))

    def test_custom_rules(self):
        p = Path(tempfile.mkdtemp()) / "f.txt"
        p.write_text("-.*secret.*\n+.\n")
        f = URLFilter(path=p)
        self.assertFalse(f.allow("https://example.com/secret/1"))
        self.assertTrue(f.allow("https://example.com/ok"))

    def test_load_skips_comments(self):
        rules = load_rules()
        self.assertTrue(rules)


if __name__ == "__main__":
    unittest.main()
