import argparse
import sys

from . import __version__
from . import config
from .paths import DB_PATH, SOLR_HOME


def main(argv=None):
    p = argparse.ArgumentParser(
        prog="sparkler",
        description="A crawl workstation: View, Control, and Crawl.",
    )
    p.add_argument("--version", action="version", version=__version__)
    sub = p.add_subparsers(dest="cmd", required=True)

    inj = sub.add_parser("inject", help="Inject seed URLs into a job")
    inj.add_argument("-id", "--id", required=True, help="Job / crawl id")
    inj.add_argument("-su", "--seed-url", action="append", default=[], help="Seed URL (repeatable)")
    inj.add_argument("-sf", "--seed-file", help="File of URLs, one per line")

    cr = sub.add_parser("crawl", help="Fetch, parse, and expand a job")
    cr.add_argument("-id", "--id", required=True)
    cr.add_argument("-tn", "--topn", type=int, default=None)
    cr.add_argument("-i", "--iterations", type=int, default=1,
                    help="Fetch batches. -1 = until the frontier is empty")
    cr.add_argument("-d", "--max-depth", type=int, default=-1,
                    help="Max link hops from a seed. -1 = unlimited")
    cr.add_argument("--same-host", action="store_true",
                    help="Stay on seed hosts (www and apex count as one site)")
    cr.add_argument("--no-expand", action="store_true",
                    help="Fetch the current frontier only; do not queue new outlinks")
    cr.add_argument("--no-robots", action="store_true")
    cr.add_argument("--delay-ms", type=int, default=None)

    srv = sub.add_parser("serve", help="API + Vue UI")
    srv.add_argument("--host", default=None)
    srv.add_argument("--port", type=int, default=None)

    rst = sub.add_parser("reset", help="Drop a job (or everything with --all)")
    rst.add_argument("-id", "--id")
    rst.add_argument("--all", action="store_true")
    rst.add_argument("--yes", action="store_true")

    sl = sub.add_parser("solr", help="Start/stop/status the private Solr CrawlDB")
    sl.add_argument("action", choices=["start", "stop", "status"])

    args = p.parse_args(argv)
    if args.cmd == "inject":
        return _inject(args)
    if args.cmd == "crawl":
        return _crawl(args)
    if args.cmd == "serve":
        return _serve(args)
    if args.cmd == "reset":
        return _reset(args)
    if args.cmd == "solr":
        return _solr(args)
    return 1


def _read_seeds(args):
    urls = list(args.seed_url)
    if args.seed_file:
        with open(args.seed_file) as f:
            for line in f:
                line = line.strip()
                if line and not line.startswith("#"):
                    urls.append(line)
    if not urls:
        print("no seeds: pass -su URL or -sf file", file=sys.stderr)
        return None
    return urls


def _inject(args):
    urls = _read_seeds(args)
    if urls is None:
        return 1
    from .crawl.loop import inject
    n = inject(args.id, urls, seed=True)
    print(f"injected {n} urls into job {args.id}")
    return 0


def _crawl(args):
    from .crawl.loop import crawl
    cfg = config.crawl_cfg()
    topn = args.topn if args.topn is not None else cfg["topn"]
    stats = crawl(
        args.id,
        topn=topn,
        iterations=args.iterations,
        same_host=args.same_host or cfg["same_host"],
        respect_robots=not args.no_robots,
        delay_ms=args.delay_ms,
        max_depth=args.max_depth,
        expand=not args.no_expand,
        on_progress=lambda p: print(f"  {p.get('url','')}", flush=True),
    )
    print(
        f"job {stats['job']}: fetched {stats['fetched']}  "
        f"errors {stats['errors']}  filtered {stats['filtered']}  "
        f"new {stats['injected']}  iterations {stats['iterations']}"
    )
    return 0


def _serve(args):
    from . import solr_server
    from .serve import run
    solr_server.ensure()
    host, port = config.serve_bind()
    host = args.host or host
    port = args.port or port
    print(f"Sparkler on http://{host}:{port}/")
    print(f"CrawlDB Solr {config.solr_base()}")
    print(f"control {DB_PATH}")
    try:
        run(host=host, port=port)
    except KeyboardInterrupt:
        print("stopped")
    return 0


def _reset(args):
    if not args.id and not args.all:
        print("pass -id JOB or --all", file=sys.stderr)
        return 1
    if not args.yes:
        target = "ALL jobs" if args.all else args.id
        ans = input(f"Drop Sparkler {target}? [y/N] ").strip().lower()
        if ans not in ("y", "yes"):
            print("aborted")
            return 1
    from . import solr_server
    from .control import store
    from .solr import CrawlDB
    solr_server.ensure()
    db = CrawlDB()
    if args.all:
        db.delete_all()
        store.reset_all()
        print(f"reset Solr {SOLR_HOME} and {DB_PATH}")
    else:
        db.delete_job(args.id)
        store.delete_job(args.id)
        print(f"reset job {args.id}")
    db.close()
    return 0


def _solr(args):
    from . import solr_server
    if args.action == "start":
        solr_server.start()
        print(f"Solr {config.solr_base()}")
    elif args.action == "stop":
        solr_server.stop()
        print("Solr stopped")
    else:
        print(solr_server.status())
        print("crawldb ping", "ok" if solr_server.is_up() else "down")
    return 0


if __name__ == "__main__":
    sys.exit(main())
