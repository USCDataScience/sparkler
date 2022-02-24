"""
Setup the API and add the required namespaces
"""

from flask_restplus import Api

from app.apis.ns_search import API as search_api
from app.apis.ns_classify import API as classify_api

API_OBJ = Api(title='Seed Generation', version='1.0',
              description='Tool to generate seeds for Domain Discovery', doc='/doc')

API_OBJ.add_namespace(search_api)
API_OBJ.add_namespace(classify_api)
