#!/bin/bash

pip install databricks-cli

/home/runner/.local/bin/databricks fs cp --recursive --overwrite build/ dbfs:/FileStore/sparkler-submit/
