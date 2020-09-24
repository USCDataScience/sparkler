#! /usr/bin/python

import logging
import sys
logging.basicConfig(stream=sys.stderr)
sys.path.insert(0, '/sce/webui')
from run import APP as application
application.secret_key = 'anything you wish'
