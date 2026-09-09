"""Download, start, and stop a private Solr 10 for Sparkler.

Does not touch DRAT (9000), ImageCat (9100), BigTranslate Solr (8985), or Meridian.
"""
from __future__ import annotations

import os
import shutil
import subprocess
import tarfile
import time
import urllib.request
from pathlib import Path

from . import config
from .paths import (
    JAVA21,
    SOLR_CONF,
    SOLR_DIST,
    SOLR_HOME,
    SOLR_TGZ,
    SOLR_URL,
    SOLR_VERSION,
    ensure_data,
)


def _java_home() -> str:
    env = os.environ.get("JAVA_HOME")
    if env and Path(env, "bin", "java").exists():
        # Solr 10 wants Java 21+. Prefer 21 if default is 11.
        ver = _java_major(Path(env))
        if ver is not None and ver >= 21:
            return env
    if JAVA21.exists():
        return str(JAVA21)
    home = subprocess.check_output(["/usr/libexec/java_home"], text=True).strip()
    return home


def _java_major(home: Path) -> int | None:
    java = home / "bin" / "java"
    if not java.exists():
        return None
    try:
        out = subprocess.check_output([str(java), "-version"], stderr=subprocess.STDOUT, text=True)
    except Exception:
        return None
    import re
    m = re.search(r'version "(\d+)', out)
    return int(m.group(1)) if m else None


def _env():
    env = os.environ.copy()
    env["JAVA_HOME"] = _java_home()
    env["PATH"] = str(Path(env["JAVA_HOME"]) / "bin") + os.pathsep + env.get("PATH", "")
    env.pop("SOLR_PORT", None)
    env.pop("SOLR_HOME", None)
    env.pop("SOLR_PID_DIR", None)
    env["SOLR_HOST_BIND"] = config.solr_host()
    env["SOLR_PORT"] = str(config.solr_port())
    env["SOLR_PID_DIR"] = str(SOLR_HOME)
    env["SOLR_LOGS_DIR"] = str(SOLR_HOME / "logs")
    env["SOLR_ULIMIT_CHECKS"] = "false"
    return env


def _bin() -> Path:
    return SOLR_DIST / "bin" / "solr"


def download():
    ensure_data()
    if _bin().exists():
        return SOLR_DIST
    SOLR_DIST.parent.mkdir(parents=True, exist_ok=True)
    if not SOLR_TGZ.exists() or SOLR_TGZ.stat().st_size < 1_000_000:
        print(f"Downloading Solr {SOLR_VERSION}…")
        urllib.request.urlretrieve(SOLR_URL, SOLR_TGZ)
    print(f"Unpacking Solr {SOLR_VERSION}…")
    with tarfile.open(SOLR_TGZ) as tf:
        try:
            tf.extractall(SOLR_DIST.parent, filter="data")
        except TypeError:
            tf.extractall(SOLR_DIST.parent)
    if not _bin().exists():
        # slim/full tarballs both unpack as solr-VERSION/
        found = next((p for p in SOLR_DIST.parent.glob("solr-*") if (p / "bin" / "solr").exists()), None)
        if found and found != SOLR_DIST:
            if SOLR_DIST.exists():
                shutil.rmtree(SOLR_DIST)
            found.rename(SOLR_DIST)
    if not _bin().exists():
        raise RuntimeError(f"Solr extract failed: {_bin()} missing")
    return SOLR_DIST


def _install_core():
    SOLR_HOME.mkdir(parents=True, exist_ok=True)
    (SOLR_HOME / "logs").mkdir(parents=True, exist_ok=True)
    src_xml = SOLR_DIST / "server" / "solr" / "solr.xml"
    dest_xml = SOLR_HOME / "solr.xml"
    if src_xml.exists() and not dest_xml.exists():
        shutil.copy2(src_xml, dest_xml)
    elif not dest_xml.exists():
        dest_xml.write_text('<?xml version="1.0" encoding="UTF-8"?>\n<solr></solr>\n')
    core = SOLR_HOME / "crawldb"
    conf = core / "conf"
    conf.mkdir(parents=True, exist_ok=True)
    src_conf = SOLR_CONF / "conf"
    for name in ("schema.xml", "solrconfig.xml", "stopwords.txt", "synonyms.txt"):
        shutil.copy2(src_conf / name, conf / name)
    props = core / "core.properties"
    if not props.exists():
        props.write_text("name=crawldb\nconfig=solrconfig.xml\nschema=schema.xml\n")


def status() -> str:
    port = config.solr_port()
    try:
        out = subprocess.check_output(
            [str(_bin()), "status"],
            env=_env(),
            text=True,
            stderr=subprocess.STDOUT,
        )
    except Exception as e:
        return f"solr not running ({e})"
    return out


def is_up() -> bool:
    from .solr import CrawlDB
    return CrawlDB().ping()


def start(wait=True):
    download()
    _install_core()
    if is_up():
        return True
    port = config.solr_port()
    host = config.solr_host()
    cmd = [
        str(_bin()), "start",
        "--user-managed",
        "--host", host,
        "-p", str(port),
        "--solr-home", str(SOLR_HOME),
        "-m", "512m",
    ]
    print(f"Starting Solr {SOLR_VERSION} on {host}:{port}")
    subprocess.check_call(cmd, env=_env())
    if not wait:
        return True
    for _ in range(40):
        if is_up():
            return True
        time.sleep(0.5)
    raise RuntimeError("Solr started but crawldb ping failed")


def stop():
    if not _bin().exists():
        return
    port = config.solr_port()
    try:
        subprocess.check_call([str(_bin()), "stop", "-p", str(port)], env=_env())
    except subprocess.CalledProcessError:
        pass


def ensure():
    start(wait=True)
