from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
DATA_DIR = ROOT / "data"
CONF_DIR = ROOT / "conf"
WEB_DIST = ROOT / "web" / "dist"
DB_PATH = DATA_DIR / "sparkler.sqlite"
SOLR_TGZ = DATA_DIR / "solr-10.0.0-slim.tgz"
SOLR_DIST = DATA_DIR / "solr-dist" / "solr-10.0.0"
SOLR_HOME = DATA_DIR / "solr"
SOLR_CONF = CONF_DIR / "solr" / "crawldb"
CONFIG_PATH = CONF_DIR / "sparkler.yaml"
FILTER_PATH = CONF_DIR / "regex-urlfilter.txt"
SEED_DEMO = ROOT / "demo" / "seeds.txt"

SOLR_VERSION = "10.0.0"
SOLR_URL = f"https://archive.apache.org/dist/solr/solr/{SOLR_VERSION}/solr-{SOLR_VERSION}-slim.tgz"
JAVA21 = Path("/opt/homebrew/Cellar/openjdk@21/21.0.12/libexec/openjdk.jdk/Contents/Home")


def ensure_data():
    DATA_DIR.mkdir(parents=True, exist_ok=True)
    return DATA_DIR
