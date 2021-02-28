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


SCRIPT_DIR: str = os.path.dirname(os.path.realpath(__file__))
REPO_ROOT: str = os.path.realpath(os.path.join(__file__, '..', '..', '..', '..', '..'))


def main() -> None:
    parser = argparse.ArgumentParser(
        description="CLI to manage docker containers",
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


def up() -> None:
    cmd = " ".join([
        f"docker-compose --file sparkler-core/sparkler-deployment/docker/elasticsearch/docker-compose.yml up --detach"
    ])

    _shell_exec_check_output(cmd, cwd = REPO_ROOT)


def down() -> None:
    cmd = " ".join([
        f"docker-compose --file sparkler-core/sparkler-deployment/docker/elasticsearch/docker-compose.yml down"
    ])

    _shell_exec_check_output(cmd, cwd = REPO_ROOT)


def _eprint(msg: str) -> None:
    sys.stderr.write("%s\n" % msg)


def _shell_exec_check_output(cmd: str, **kwargs: Any) -> None:
    _eprint(f"Exec: {cmd}")
    subprocess.run(cmd, check=True, shell=True, **kwargs)


if __name__ == "__main__":
    main()
