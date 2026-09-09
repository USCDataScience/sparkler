import unittest
from sparkler.crawl.urls import (
    contenthash, doc_id, group_of, host_key, hostname, normalize, page_family, same_page_family,
)


class UrlTests(unittest.TestCase):
    def test_normalize_strips_fragment(self):
        self.assertEqual(normalize("https://Example.com/a#x"), "https://example.com/a")

    def test_canonical_www_https(self):
        self.assertEqual(normalize("http://www.mattmann.ai/about/"), "https://mattmann.ai/about")
        self.assertEqual(
            doc_id("mai", "http://www.mattmann.ai/about"),
            doc_id("mai", "https://mattmann.ai/about/"),
        )

    def test_pagination_same_family(self):
        a = "https://mattmann.ai/blog"
        b = "https://www.mattmann.ai/blog?e-page-48118d4=2"
        self.assertEqual(page_family(a), page_family(b))
        self.assertTrue(same_page_family(normalize(b), normalize(a)))
        self.assertFalse(same_page_family("https://mattmann.ai/about", a))

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

    def test_skips_phone_as_port(self):
        self.assertIsNone(normalize("http://adminvc.ucla.edu:+1-310-825-4321"))
        self.assertIsNone(normalize("tel:+1-310-825-4321"))


if __name__ == "__main__":
    unittest.main()
