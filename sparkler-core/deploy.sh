#!/bin/bash

method=$1

pip install databricks-cli

if [ "$method" = "standalone" ]; then
  rm -rf build/sparkler-app-0.3.1-SNAPSHOT
  mv build sparkler
  zip -r sparkler.zip sparkler
  export DATABRICKS_HOST=$DEV_DATABRICKS_HOST
  export DATABRICKS_TOKEN=$DEV_DATABRICKS_TOKEN
  ~/.local/bin/databricks fs cp --recursive --overwrite sparkler.zip dbfs:/FileStore/sparkler-standalone/
  export DATABRICKS_HOST=$NEWDEV_DATABRICKS_HOST
  export DATABRICKS_TOKEN=$NEWDEV_DATABRICKS_TOKEN
  ~/.local/bin/databricks fs cp --recursive --overwrite sparkler.zip dbfs:/FileStore/sparkler-standalone/
  export DATABRICKS_HOST=$TEST_DATABRICKS_HOST
  export DATABRICKS_TOKEN=$TEST_DATABRICKS_TOKEN
  ~/.local/bin/databricks fs cp --recursive --overwrite sparkler.zip dbfs:/FileStore/sparkler-standalone/
else
  rm -rf build/sparkler-app-0.3.1-SNAPSHOT
  export DATABRICKS_HOST=$DEV_DATABRICKS_HOST
  export DATABRICKS_TOKEN=$DEV_DATABRICKS_TOKEN
  ~/.local/bin/databricks fs cp --recursive --overwrite build/ dbfs:/FileStore/sparkler-submit/
  export DATABRICKS_HOST=$NEWDEV_DATABRICKS_HOST
  export DATABRICKS_TOKEN=$NEWDEV_DATABRICKS_TOKEN
  ~/.local/bin/databricks fs cp --recursive --overwrite build/ dbfs:/FileStore/sparkler-submit/
  export DATABRICKS_HOST=$TEST_DATABRICKS_HOST
  export DATABRICKS_TOKEN=$TEST_DATABRICKS_TOKEN
  ~/.local/bin/databricks fs cp --recursive --overwrite build/ dbfs:/FileStore/sparkler-submit/
fi
