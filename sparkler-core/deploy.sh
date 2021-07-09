#!/bin/bash

method=$1

pip install databricks-cli

if [ "$method" = "standalone" ]; then
  rm -rf build/sparkler-app-0.3.1-SNAPSHOT
  mv build sparkler
  zip -r sparkler.zip sparkler
  ~/.local/bin/databricks fs cp --recursive --overwrite sparkler.zip dbfs:/FileStore/sparkler-standalone/
else
  rm -rf build/sparkler-app-0.3.1-SNAPSHOT
  ~/.local/bin/databricks fs cp --recursive --overwrite build/ dbfs:/FileStore/sparkler-submit/
fi
