"""Bootstrap the API"""

import logging

from flask import Flask
# Import a module / component using its blueprint handler variable
from app.controller import MOD_APP as app_module
# Import flask and template operators
from app.apis import API_OBJ

# Define the WSGI application object
APP = Flask(__name__,
            static_url_path='',
            static_folder='static')

logging.basicConfig(level=logging.DEBUG)

# Configurations
APP.config.from_object('config')


# Register blueprint(s)
APP.register_blueprint(app_module)


# Initialize flask-restplus
API_OBJ.init_app(APP)
