"""
Classify Endpoints for the REST API.
"""
import json
import os

from flask_restplus import Namespace, Resource
from flask import request
from app import classifier

PFX = os.getenv('API_PFX', '')


API = Namespace('classify', description='Interact with the ML model', path=PFX+'/classify')


@API.route('/predict', methods=['GET', 'POST'])
class Predict(Resource):
    """Predict a result"""
    @API.doc('predict')
    @staticmethod
    def get(content, model):
        """
        Predict using ML model
        :param content:
        :param model:
        :return:
        """
        classes = {
            -1: 'Model doesn\'t exist',
            0: 'Not Relevant',
            1: 'Relevant',
            2: 'Highly Relevant'
        }
        args = request.args
        if len(args) != 0:
            content = args['content']
        if content:
            result = classifier.predict(model, content)
            return classes[result]
        print('NO CONTENT FOUND')
        return classes[-1]

    @API.doc('predict')
    @staticmethod
    def post():
        """
        Predict using ML model
        :return:
        """
        classes = {
            -1: 'Model doesn\'t exist',
            0: 'Not Relevant',
            1: 'Relevant',
            2: 'Highly Relevant'
        }
        result = -1
        data = request.data
        loaded_data = json.loads(data.decode('utf-8', 'ignore'))
        if len(data) != 0:
            content = loaded_data['score'][0]['content']
            if content is None:
                return classes[-1]

            model = loaded_data['score'][0]['model']
            result = classifier.predict(model, content)
        return classes[result]
