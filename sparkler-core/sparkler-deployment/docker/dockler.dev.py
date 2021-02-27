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


REPO_ROOT: str = os.path.realpath(os.path.join(__file__, '..', '..', '..', '..'))


def main() -> None:
    parser = argparse.ArgumentParser(
        description="CLI to manage docker containers for development",
    )

    parser.add_argument(
        "--build",
        help="Builds a dev image",
        default=False,
        action="store_true",
    )

    parser.add_argument(
        "--run",
        help="Run a dev container",
        default=False,
        action="store_true",
    )

    parser.add_argument(
        "--login",
        help="Login to a running dev container",
        default=False,
        action="store_true",
    )

    parser.add_argument(
        "--clean",
        help="Remove all dev images and containers",
        default=False,
        action="store_true",
    )

    parsed_args = parser.parse_args()

    if parsed_args.clean:
        clean()
    if parsed_args.build:
        build()
    if parsed_args.run:
        run_container()
    if parsed_args.login:
        login()


def build() -> None:
    cmd = " ".join([
        "docker build",
        "--tag sparkler-dev:latest",
        "--file sparkler-core/sparkler-deployment/docker/Dockerfile.dev",
        f"{REPO_ROOT}/sparkler-core",
    ])

    _shell_exec_check_output(cmd, cwd = REPO_ROOT)


def run_container() -> None:
    cmd = " ".join([
        "docker run",
        "--detach",
        "--name sparkler-dev",
        f"--volume '{REPO_ROOT}/sparkler-core:/data/sparkler-core'",
        "--publish 8983:8983",
        "--publish 4041:4041",
        "sparkler-dev:latest",
    ])

    _shell_exec_check_output(cmd, cwd = REPO_ROOT)
    print("Container running. Login via 'docker exec -it sparkler-dev /bin/bash'")


def login() -> None:
    cmd = " ".join([
        "docker exec -it sparkler-dev /bin/bash"
    ])

    _shell_exec_check_output(cmd, cwd = REPO_ROOT)


def clean() -> None:
    cmd = " ".join([
        "docker container stop sparkler-dev;",
        "docker container rm sparkler-dev;",
        "docker image rm sparkler-dev:latest",
    ])

    _shell_exec_check_output(cmd, cwd = REPO_ROOT)


def _eprint(msg: str) -> None:
    sys.stderr.write("%s\n" % msg)


def _shell_exec_check_output(cmd: str, **kwargs: Any) -> None:
    _eprint(f"Exec: {cmd}")
    result = subprocess.run(cmd, shell=True, **kwargs)
    if result.returncode != 0:
        sys.exit(result.returncode)


if __name__ == "__main__":
    main()