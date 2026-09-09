import unittest
from sparkler.crawl.urls import contenthash, doc_id, group_of, host_key, hostname, normalize


class UrlTests(unittest.TestCase):
    def test_normalize_strips_fragment(self):
        self.assertEqual(normalize("https://Example.com/a#x"), "https://example.com/a")

    def test_normalize_rejects_mailto(self):
        self.assertIsNone(normalize("mailto:a@b.com"))

    def test_normalize_relative(self):
        self.assertEqual(
            normalize("/next", base="https://example.com/a"),
            "https://example.com/next",
        )

    def test_id_stable(self):
        a = doc_id("news", "https://example.com/")
        b = doc_id("news", "https://example.com/")
        self.assertEqual(a, b)
        self.assertNotEqual(a, doc_id("other", "https://example.com/"))

    def test_group_is_host(self):
        self.assertEqual(group_of("https://www.bbc.com/news"), "bbc.com")
        self.assertEqual(hostname("https://WWW.BBC.com/news"), "www.bbc.com")
        self.assertEqual(host_key("https://www.mattmann.ai/about"), "mattmann.ai")
        self.assertEqual(host_key("http://mattmann.ai/"), "mattmann.ai")

    def test_contenthash(self):
        self.assertEqual(len(contenthash(b"hello")), 40)


if __name__ == "__main__":
    unittest.main()
