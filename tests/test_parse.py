import unittest
from sparkler.crawl.parse import html_links


class ParseTests(unittest.TestCase):
    def test_hrefs_and_title(self):
        html = """<html><head><title>Hello</title></head>
        <body><a href="/next">n</a><a href="https://example.com/a">a</a>
        <a href="mailto:x@y.com">m</a></body></html>"""
        title, links, text = html_links(html, "https://example.com/")
        self.assertEqual(title, "Hello")
        self.assertIn("https://example.com/next", links)
        self.assertIn("https://example.com/a", links)
        self.assertTrue(all(not u.startswith("mailto:") for u in links))
        self.assertIn("n", text)


if __name__ == "__main__":
    unittest.main()
