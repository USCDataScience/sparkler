"""
Manage models in ArangoDB from a common interface.
"""

import os

from pyArango.connection import Connection
from pyArango.theExceptions import DocumentNotFoundError
from flask import current_app as app

AURL = os.getenv('ARANGO_URL', 'http://single-server-int:8529')
CONN = Connection(AURL, 'root', '', verify=False)

def get_database():
    """
    Get the database object
    :return: The database
    """
    if not CONN.hasDatabase('sce'):
        database = CONN.createDatabase('sce')
    else:
        database = CONN['sce']

    return database

def get_connection():
    """
    Get a connection and list of models.
    :return:
    """
    if not CONN.hasDatabase('sce'):
        database = CONN.createDatabase('sce')
    else:
        database = CONN['sce']

    if not database.hasCollection('models'):
        models = database.createCollection('Collection', name='models')
    else:
        models = database.collections['models']
    return models


def set_sparkler_defaults(model):
    """
    Create a model with the defaul options.
    :param model:
    :return:
    """
    topn = 1000
    topgrp = 256
    sortby = 'discover_depth asc, score asc'
    groupby = 'group'
    serverdelay = 1000
    fetchheaders = [{'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6)'
                                   ' AppleWebKit/537.36 (KHTML, like Gecko) Sparkler'},
                    {'Accept': 'text/html,application/xhtml+xml,'
                               'application/xml;q=0.9,image/webp,*/*;q=0.8'},
                    {'Accept-Language': 'en-US,en'}]

    activeplugins = ['urlfilter-regex', 'urlfilter-samehost', 'scorer-dd-svn']
    plugins = {'urlfilter.regex': {'urlfilter.regex.file': 'regex-urlfilter.txt'},
               'fetcher.jbrowser': {'socket.timeout': 3000, 'connect.timeout': 3000},
               'scorer.dd.svn': {'scorer.dd.svn.url': 'http://sce-ui/explorer-api/classify/predict',
                                 'scorer.dd.svn.fallback': 0, 'scorer.dd.svn.key': 'svn_score'}}

    content = {'crawldb.uri': 'http://sce-solr:8983/solr/crawldb',
               'spark.master': 'local[*]',
               'kafka.enable': 'false',
               'kafka.listeners': 'localhost:9092',
               'kafka.topic': 'sparkler_%s',
               'generate.topn': topn,
               'generate.top.groups': topgrp,
               'generate.sortby': sortby,
               'generate.groupby': groupby,
               'fetcher.server.delay': serverdelay,
               'fetcher.headers': fetchheaders,
               'plugins.active': activeplugins,
               'plugins': plugins}
    set_sparkler_options(model, content)


def set_sparkler_options(model, content):
    """
    Create a new set of sparkler options for the model.
    :param model:
    :param content:
    :return:
    """
    try:
        db_model = get_connection()[model]
        db_model['sparkler_opts'] = content
        db_model.save()
    except DocumentNotFoundError as error:
        app.logger.info(error)
        raise


def get_sparkler_options(model):
    """
    Get the current sparkler options.
    :param model:
    :return:
    """
    try:
        db_model = get_connection()[model]
        return db_model['sparkler_opts']
    except DocumentNotFoundError as error:
        app.logger.info(error)
        raise


def update_seed_urls(model, urls):
    """
    Update the seed urls for a model.
    :param model:
    :param urls:
    :return:
    """''
    try:
        db_model = get_connection()[model]
        old_urls = db_model['seeds']
        stringurls = []
        for url in urls:
            stringurls.append(url.decode('utf-8'))

        if old_urls is None:
            old_urls = []
        old_urls = old_urls + stringurls
        db_model['seeds'] = list(dict.fromkeys(old_urls))
        db_model.save()
        return old_urls
    except DocumentNotFoundError as error:
        app.logger.info(error)
        raise


def fetch_seeds(model):
    """
    Get the seed urls for a model.
    :param model:
    :return:
    """
    try:
        db_model = get_connection()[model]
        old_urls = db_model['seeds']
        return old_urls
    except DocumentNotFoundError as error:
        app.logger.info(error)
        raise

def get_models():
    """
    Get a list of models
    :return:
    """
    aql = 'FOR model IN models RETURN {name: model.name}'
    query_result = get_database().AQLQuery(aql, rawResults=True, batchSize=1)
    return list(query_result)
