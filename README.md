Sparkler
========

<p align="center">
  <img src="web/public/sparkler-mark.png" width="128" height="128" alt="Sparkler mark: an ember spark inside a crawl orbit."/>
</p>

A crawl workstation: **View**, **Control**, and **Crawl**.

Inject seeds. Fetch politely. Parse with Tika. Store the frontier and pages
in a Solr CrawlDB. Label pages relevant / not / highly so the next generate
pass prefers the right hosts. A Vue 3 UI is the View; FastAPI is Control;
the Python fetch loop is Crawl.

The mark is an ember in a crawl orbit — the spark is the page being fetched,
the nodes on the ring are the frontier. Compass-rose geometry for navigating
the web (search, Tika, Solr), not a campus seal. Named as Spark-Crawler;
this tree still crawls, it just does it as a single workstation.

No Docker. No Spark. No Elasticsearch. No ArangoDB.

Needs **Java 21+** (Solr 10), **Java 11+** is enough for Tika if 21 is on
`PATH` as `JAVA_HOME` for Solr, **Python 3.10+**, and **Node 18+** to build
the UI. Solr is downloaded on first use into `data/` (gitignored) and bound
to `127.0.0.1:8983`. The app is `127.0.0.1:8180`.

```bash
python3.12 -m venv .venv
.venv/bin/pip install -e .
cd web && npm install && npm run build && cd ..

bin/sparkler inject -id demo -sf demo/seeds.txt
bin/sparkler crawl  -id demo -tn 10 -i 1
bin/sparkler serve
```

Open http://127.0.0.1:8180/

```bash
bin/sparkler inject -id news -su 'https://example.com/'
bin/sparkler crawl  -id news -i 2 --same-host
bin/sparkler reset  -id news --yes
bin/sparkler solr   status
```

`bin/sparkler` uses `.venv` when it exists.

Inspired by USC IRDS Sparkler.
