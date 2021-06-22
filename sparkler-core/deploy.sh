#!/bin/bash

pip install databricks-cli

~/.local/bin/databricks fs cp --recursive --overwrite build/ dbfs:/FileStore/sparkler-submit/
