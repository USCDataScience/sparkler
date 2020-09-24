"""
Search Endpoints for the REST API
"""
import json
import os

from flask_restplus import Namespace, Resource, cors
from flask import current_app as a
from pyArango.theExceptions import DocumentNotFoundError
from werkzeug.exceptions import BadRequest
from app import search

PFX = os.getenv('API_PFX', '')

API = Namespace('search', description='Query Duck Duck Go for results', path=PFX+'/search')


@API.route('/<model>/<query>')
@API.param('query', 'Query string to search')
class Search(Resource):
    """ Search a resource """
    @classmethod
    @API.doc('search')
    @cors.crossdomain(origin='*')
    def get(cls, model, query):
        """
        Search Duck Duck Go
        :param model:
        :param query:
        :return:
        """
        a.logger.debug('Search Called!')
        try:
            url_details = search.query_and_fetch(query, model, top_n=12)
        except DocumentNotFoundError as exception:
            print(exception)
            raise BadRequest('Model Not Found')

        return json.dumps(url_details)


@API.route('/<model>/<query>/<page>')
@API.param('query', 'Query string to search')
@API.param('page', 'Results Page')
class SearchPaginated(Resource):
    """Execute a paginated search"""
    @classmethod
    @API.doc('searchpaginated')
    @cors.crossdomain(origin='*')
    def get(cls, model, query, page):
        """
        Search Duck Duck Go
        :param model:
        :param query:
        :param page:
        :return:
        """
        a.logger.debug('Paged Search Called!')
        try:
            url_details = search.query_and_fetch(query, model, page=int(page), top_n=12)
        except DocumentNotFoundError as exception:
            print(exception)
            raise BadRequest('Model Not Found')

        return json.dumps(url_details)
