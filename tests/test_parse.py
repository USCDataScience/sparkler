import unittest
from sparkler.crawl.parse import clean_metadata, html_links, md_field, solr_md_fields


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

    def test_tika_keys_flatten(self):
        md = clean_metadata({
            "Content-Type": "text/html",
            "dc:title": ["Example"],
            "X-TIKA:Parsed-By": ["a", "b"],
            "empty": "",
        })
        self.assertEqual(md["Content-Type"], "text/html")
        self.assertEqual(md["dc:title"], "Example")
        self.assertEqual(md["X-TIKA:Parsed-By"], ["a", "b"])
        self.assertNotIn("empty", md)
        fields = solr_md_fields(md)
        self.assertEqual(fields["Content_Type_s_md"], ["text/html"])
        self.assertEqual(md_field("X-TIKA:Parsed-By"), "X_TIKA_Parsed_By_s_md")


if __name__ == "__main__":
    unittest.main()
