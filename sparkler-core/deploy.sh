#!/bin/bash

pip install databricks-cli

rm -rf build/sparkler-app-0.3.1-SNAPSHOT
#rm -rf build/plugins

~/.local/bin/databricks fs cp --recursive --overwrite build/ dbfs:/FileStore/sparkler-submit/
