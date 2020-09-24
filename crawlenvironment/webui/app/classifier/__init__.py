"""
The classifier is designed to classify text injected into the platform via a REST API.
This will update the model and provide predictions as to the relevance of the
crawled pages.
"""
import json
import os
import pickle

from sklearn.feature_extraction.text import CountVectorizer, TfidfTransformer
from sklearn.externals import joblib
from sklearn.ensemble import RandomForestClassifier

import numpy as np
import flask
from flask import request, flash, current_app as app

from app.models.model import set_sparkler_defaults, get_connection

STORAGE_LOCATION = os.getenv('STORAGE_LOCATION', '')

def load_vocab():
    """Load Vocabulary"""
    if os.path.exists('keywords.txt'):
        with open('keywords.txt', 'rb') as file:
            keywords_content = file.read()
    else:
        with open('keywords.txt', 'wb') as fwrite:
            fwrite.write('This is a test')
            keywords_content = 'This is a test'
    count_vect = CountVectorizer(lowercase=True, stop_words='english')
    count_vect.fit_transform([keywords_content])
    keywords = count_vect.vocabulary_
    return keywords



def new_model(name_label):
    """
    Create a new model
    :param name:
    :return:
    """
    obj = {
        'name': name_label,
        '_key': name_label
    }
    model = get_connection().createDocument(obj)
    model.save()
    set_sparkler_defaults(name_label)
    print('create new model')



def update_model(model_name, annotations):
    """
    Update a model
    :param model_name:
    :param annotations:
    :return:
    """

    model = get_connection()[model_name]

    if model['url_text'] is not None:
        url_text = model['url_text']
    else:
        url_text = None

    if model['url_details'] is not None:
        url_details = model['url_details']
    else:
        url_details = None
    # clf = MLPClassifier(max_iter=1000, learning_rate='adaptive',)
    clf = RandomForestClassifier(n_estimators=100)
    count_vect = CountVectorizer(lowercase=True, stop_words='english')
    tfidftransformer = TfidfTransformer()

    if url_text is None:
        print('An error occurred while accessing the application context variables')
        return '-1'

    labeled = np.array(annotations)

    if model['labeled'] is not None:
        # add the old docs to the new
        prev_url_text = model['url_text']
        prev_labeled = model['labeled']
        prev_url_details = model['url_details']
        url_text = np.append(url_text, prev_url_text, axis=0)
        labeled = np.append(labeled, prev_labeled, axis=0)
        url_details = np.append(url_details, prev_url_details, axis=0)


    features = count_vect.fit_transform(url_text)
    features = tfidftransformer.fit_transform(features).toarray().astype(np.float64)

    print('No. of features: ' + str(len(features)) + ' and No. of labels: ' + str(len(labeled)))

    print(np.unique(labeled))
    clf.fit(features, labeled, )

    # save the model
    model['url_text'] = url_text
    if isinstance(url_details, np.ndarray):
        model['url_details'] = url_details.tolist()
    else:
        model['url_details'] = url_details
    model['labeled'] = labeled.tolist()
    encoded_model = {'countvectorizer': count_vect, 'tfidftransformer': tfidftransformer,
                     'clf': clf}
    with open(STORAGE_LOCATION+'/models/' + model['name'] + '.pickle', 'wb') as handle:
        pickle.dump(encoded_model, handle, protocol=pickle.HIGHEST_PROTOCOL)
    model.save()
    #predicted = clf.predict(features)
    #accuracy = (labeled == predicted).sum() / float(len(labeled))

    return json.dumps(whack_a_mole_model(get_metrics(model)))


def get_metrics(model):
    """
    Get the metrics
    :param model:
    :return:
    """
    unique, counts = np.unique(model['labeled'], return_counts=True)
    dictionary = dict(zip(unique, counts))

    return dictionary


def predict(model_name, txt):
    """
    Run a prediction
    :param model_name:
    :param txt:
    :return:
    """
    model = get_connection()[model_name]
    encoded_model = {}

    if os.path.isfile(STORAGE_LOCATION+'/models/' + model['name'] + '.pickle'):
        with open(STORAGE_LOCATION+'/models/' + model['name'] + '.pickle', 'rb') as handle:
            encoded_model = pickle.load(handle)

    # if 'countvectorizer' in encoded_model:
    #     test = encoded_model['countvectorizer']
    if model is None:
        app.logger.info('Model not found')
        return -1
    if 'countvectorizer' not in encoded_model or encoded_model['countvectorizer'] is None:
        app.logger.info('No Count Vectorizer')
        return -1

    app.logger.info('Sorting Count Vectorizer out ' + model['name'])
    count_vect = encoded_model['countvectorizer']
    tfidftransformer = encoded_model['tfidftransformer']
    clf = encoded_model['clf']

    features = count_vect.transform([txt])
    features = tfidftransformer.transform(features).toarray().astype(np.float64)

    predicted = clf.predict(features)
    return predicted[0]


def import_model(model_name):
    """
    Import a model
    :param model_name:
    :return:
    """
    print('importing model: '+model_name)
    print('importing')
    filename = 'model.pkl'

    if 'file' not in request.files:
        flash('No file part')
        return '-1'
    file = request.files['file']
    if file.filename == '':
        flash('No selected file')
        return '-1'
    if file:
        file.save(os.path.join(flask.current_app.root_path,
                               flask.current_app.config['UPLOAD_FOLDER'], filename))
    else:
        flash('An error occurred while uploading the file')
        return '-1'

    model = joblib.load(os.path.join(flask.current_app.root_path,
                                     flask.current_app.config['UPLOAD_FOLDER'], filename))

    #accuracy = model['accuracy']

    setattr(flask.current_app, 'model', model)

    dictionary = get_metrics(model)
    dictionary = whack_a_mole_model(dictionary)
    json_dictionary = json.dumps(dictionary)

    return json_dictionary


def export_model(model_name):
    """
    Export a model
    :param model_name:
    :return:
    """
    return flask.send_from_directory(directory=STORAGE_LOCATION+'/models/',
                                     filename=model_name + '.pickle')


def check_model(model_name):
    """
    Check a model
    :param model_name:
    :return:
    """
    model = get_connection()[model_name]
    if model is None:
        return str(-1)

    dictionary = get_metrics(model)

    dictionary = whack_a_mole_model(dictionary)


    return flask.jsonify(dictionary)


def whack_a_mole_model(dictionary):
    """
    Too lazy to do it properly, but we need to replace numpy types in dicts.
    :param dictionary:
    :return:
    """
    if dictionary is not None:
        try:
            dictionary = {int(k):int(v) for k, v in dictionary.items()}
        except TypeError:
            dictionary = {str(k):int(v) for k, v in dictionary.items()}

    return dictionary
