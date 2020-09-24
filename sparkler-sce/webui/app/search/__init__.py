"""
Execute various query and crawl operations.
"""
import base64
import logging
import urllib
import os
import uuid
import requests


from bs4 import BeautifulSoup
from flask import current_app as app
from pyArango.theExceptions import DocumentNotFoundError


from app.classifier import predict

from app.models.model import get_connection

from app.search.fetcher import Fetcher

logging.basicConfig(level=logging.DEBUG)
URL_DETAILS = []
URL_TEXT = []

SCEHOST = os.getenv('SCE_HOST', 'http://sce-splash:8050')
STORAGE_LOCATION = os.getenv('STORAGE_LOCATION', '')

def get_url_window(query, top_n, page):
    """
    Get the contents of a duck duck go query.
    :param query:
    :param top_n:
    :param page:
    :return:
    """
    bad_request = False
    start_pos = top_n * (page - 1)
    end_pos = start_pos + top_n
    output = ''
    app.logger.info('Processing get url request ' + query)
    query = urllib.parse.quote(query)
    search_url = SCEHOST + '/render.html?url=https%3A%2F%2Fduckduckgo.com' \
                 '%2F%3Fq%3D' + query + '%26kl%3Dwt-wt%26ks%3Dl%26k1%3D-1%26kp%3D-1%2' \
                 '6ka%3Da%26kaq%3D-1%26k18%3D-1%26kax%3D-1%26kaj%3Du%26kac%3D-1%26kn%' \
                 '3D1%26kt%3Da%26kao%3D-1%26kap%3D-1%26kak%3D-1%26kk%3D-1%26ko%3Ds%26' \
                 'kv%3D-1%26kav%3D1%26t%3Dhk%26ia%3Dnews&wait=5'
    try:
        output = requests.get(search_url).content
    except requests.exceptions.RequestException as exception:
        app.logger.info('An error occurred while searching query: ' + query)
        app.logger.info(exception)
        bad_request = True

    #try:
    if not bad_request:
        soup = BeautifulSoup(output, 'html.parser')
        results = soup.findAll('a', {'class': 'result__a'})
        result_size = len(results)
        app.logger.info('Results Found ' + str(result_size))
        return results[start_pos:end_pos]
    #except Exception as exception:
    #    app.logger.info(exception)
    #    print('An error occurred while searching query: ' + query + ' and fetching results')

    return []

def parse_details(model, fetched_data):
    """
    Parse query details
    :param model:
    :param fetched_data:
    :return:
    """
    details = dict()
    details['url'] = fetched_data[0]
    details['html'] = fetched_data[1]
    details['title'] = str(fetched_data[2])
    details['label'] = int(predict(model, fetched_data[3]))

    imag = None
    try:
        imag = requests.get(SCEHOST+'/render.png?url='
                            + fetched_data[0] +
                            '&wait=5&width=320&height=240')
    except requests.exceptions.ConnectionError:
        print('Connection error to SCE')

    if imag is not None and imag.status_code == 200:
        generatedid = str(uuid.uuid4())
        with open(STORAGE_LOCATION+'/images/' + generatedid + '.png', 'wb') as file:
            file.write(imag.content)
        details['image'] = STORAGE_LOCATION+'/images/' + generatedid + '.png'

        with open(details['image'], 'rb') as image_file:
            encoded_string = base64.b64encode(image_file.read())
            details['image'] = encoded_string.decode()


    return details

def update_model(model, url_text, url_details):
    """
    Update the search model
    :param model:
    :param url_text:
    :param url_details:
    :return:
    """
    try:
        dbmodel = get_connection()[model]
        dbmodel['url_text'] = url_text
        dbmodel['url_details'] = url_details
        dbmodel.save()
    except DocumentNotFoundError as error:
        app.logger.info(error)
        raise


def query_and_fetch(query, model, top_n=12, page=1):
    """
    Query a result list and fetch the data within them.
    :param query:
    :param model:
    :param top_n:
    :param page:
    :return:
    """
    app.logger.debug('Query: ' + query + '; Top N: ' + str(top_n))
    url_details = []
    url_text = []
    bad_request = False

    results = get_url_window(query, top_n, page)

    if not bad_request:
        urls = []
        for element in results:
            urls.append(element['href'])

        fetched_result = Fetcher.fetch_multiple(urls, None)
        for fetched_data in fetched_result:
            if fetched_data is not None:
                if len(url_details) == 12:
                    break

                if not fetched_data[1] or len(fetched_data[1].strip()) == 0:
                    app.logger.info('Continuing')
                    continue
                app.logger.info('Extracting URL: ' + fetched_data[0])
                url_details.append(parse_details(model, fetched_data))
                url_text.append(fetched_data[3])


    update_model(model, url_text, url_details)

    print('Search Completed')
    app.logger.info('Search Completed')

    return url_details
