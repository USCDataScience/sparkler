import yaml
from .paths import CONFIG_PATH


def load():
    with open(CONFIG_PATH) as f:
        return yaml.safe_load(f) or {}


def solr_base():
    s = load()["solr"]
    return f"http://{s['host']}:{s['port']}/solr/{s['core']}"


def solr_port():
    return int(load()["solr"]["port"])


def solr_host():
    return load()["solr"]["host"]


def serve_bind():
    s = load()["serve"]
    return s["host"], int(s["port"])


def crawl_cfg():
    return dict(load()["crawl"])
