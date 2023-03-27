#!/usr/bin/env python3
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.


import argparse
import os
import subprocess
import sys
from typing import Any
from pathlib import Path
import logging as log


log.basicConfig(level=log.INFO)
SCRIPT_DIR: Path = Path(__file__).parent
#REPO_ROOT: Path = SCRIPT_DIR.parent.parent.parent
COMPOSE_FILE: Path = (SCRIPT_DIR / "docker-compose.yml").resolve()


EPILOG = """
For docs, visit 
"""
def main() -> None:
    parser = argparse.ArgumentParser(
        description="CLI to manage docker containers",
        formatter_class=argparse.ArgumentDefaultsHelpFormatter,
        epilog=EPILOG
    )

    parser.add_argument(
        "--compose", type=Path,
        help="docker-compose.yml file to use",
        default=str(COMPOSE_FILE),
    )

    parser.add_argument(
        "--up",
        help="Brings the service up",
        default=False,
        action="store_true",
    )

    parser.add_argument(
        "--down",
        help="Brings the service down and cleans up containers",
        default=False,
        action="store_true",
    )

    parser.add_argument(
        "--login",
        help="Login to a running container",
        default=False,
        action="store_true",
    )

    parsed_args = parser.parse_args()

    if parsed_args.up:
        up()
    if parsed_args.down:
        down()
    if parsed_args.login:
        login()


def login() -> None:
    msg = "\n".join([
        "Login to any of the running service containers by running:",
        "  docker exec -it sparkler-elastic /bin/bash",
        "  docker exec -it elasticsearch /bin/bash",
        "  docker exec -it kibana /bin/bash",
    ])
    print(msg)


def up(compose_file=COMPOSE_FILE) -> None:
    cmd = f"docker compose --file {compose_file} up --detach"
    try:
        _shell_exec_check_output(cmd)
    except Exception:
        log.error(f"Exec failed. If docker-compose is missing, follow the instructions here to install: https://github.com/USCDataScience/sparkler/wiki/docker-compose")
        sys.exit(1)


def down(compose_file=COMPOSE_FILE) -> None:
    cmd = f"docker compose --file {compose_file} down"

    try:
        _shell_exec_check_output(cmd)
    except Exception:
        log.error(f"Exec failed. If docker-compose is missing, follow the instructions here to install: https://github.com/USCDataScience/sparkler/wiki/docker-compose")
        sys.exit(1)


def _shell_exec_check_output(cmd: str, **kwargs: Any) -> None:
    log.info(f"Exec: {cmd}")
    subprocess.run(cmd, check=True, shell=True, **kwargs)


if __name__ == "__main__":
    main()
