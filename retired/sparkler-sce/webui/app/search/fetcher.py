"""Fetching Capability using requests"""
import logging
import requests
from bs4 import BeautifulSoup
from datetime import datetime
from flask import current_app as app

logging.getLogger('urllib3').setLevel(logging.WARNING)
logging.getLogger('chardet').setLevel(logging.WARNING)


class Fetcher:
    """Fetching Capability using requests"""

    search_driver = None
    screenshot_driver = None

    @staticmethod
    def cleantext(soup):
        """
        Clean up the text from the extract
        :param soup:
        :return:
        """
        for script in soup(['script', 'style']):
            script.extract()  # rip it out
        text = soup.get_text()
        lines = (line.strip() for line in text.splitlines())
        chunks = (phrase.strip() for line in lines for phrase in line.split('  '))
        text = '\n'.join(chunk for chunk in chunks if chunk)
        text = text.replace('\n', ' ')
        #return text.encode('utf-8')
        return text

    @staticmethod
    def clean_string(text):
        """
        Clean up a single string
        :param text:
        :return:
        """
        try:
            #lines = (line.strip() for line in text.splitlines())
            #chunks = (phrase.strip() for line in lines for phrase in line.split('  '))
            lines = []
            chunks = []
            for line in text.splitlines():
                line = line.strip()
                lines.append(line)

            for line in lines:
                for phrase in line.split(' '):
                    phrase = phrase.strip()
                    chunks.append(phrase)

            text = '\n'.join(chunk for chunk in chunks if chunk)
            text = text.replace('\n', ' ')
            return str(text)
        except TypeError:
            return text

    @staticmethod
    def read_url2(url):
        """
        Read teh supplied url
        :param url:
        :return:
        """
        # res = urlopen(url)
        # data = res.read()
        data = None
        now = datetime.now()
        current_time = now.strftime("%H:%M:%S")
        app.logger.debug('Fetching Content: ' +current_time)
        try:
            data = requests.get(url, timeout=5).content
        except requests.exceptions.ConnectionError:
            app.logger.debug('Connection Failed')

        if data is not None:
            imag = None
            now = datetime.now()
            current_time = now.strftime("%H:%M:%S")
            app.logger.debug('Fetching Soup: '+ current_time)
            print('Fetched %s from %s' % (len(data), url))
            # if res.headers.getparam('charset').lower() != 'utf-8':
            #    data = data.encode('utf-8')
            soup = BeautifulSoup(data, 'html.parser')
            print('Parsed %s from %s' % (len(data), url))
            now = datetime.now()
            current_time = now.strftime("%H:%M:%S")
            app.logger.debug('Cleaning Soup: '+ current_time)
            clean_url = Fetcher.clean_string(url)
            clean_data = Fetcher.clean_string(soup.prettify())

            title = ''
            if soup.title is not None:
                title = soup.title.string

            clean_text = Fetcher.cleantext(soup)
            now = datetime.now()
            current_time = now.strftime("%H:%M:%S")
            app.logger.debug('Returning Soup: '+ current_time)
            return ([clean_url, clean_data, title, clean_text])

        return None

    @staticmethod
    def fetch_multiple(urls, top_n):
        """
        Fetch a bunch of urls
        :param urls:
        :param top_n:
        :return:
        """
        # result = Fetcher.parallel(urls, top_n)
        print(top_n)
        result = []
        for url in urls:
            result.append(Fetcher.read_url2(url))
        return result
