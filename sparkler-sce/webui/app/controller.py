"""Controller API routes"""
import os
import subprocess
import json

import requests
from flask import Blueprint, request, send_from_directory, jsonify
from flask_cors import CORS
import yaml
from app import classifier

# Define Blueprint(s)
from app.models import model as model_controller
from app.models.model import get_sparkler_options, set_sparkler_options, \
    update_seed_urls, fetch_seeds


PFX = os.getenv('API_PFX', '/')
MOD_APP = Blueprint('application', __name__, url_prefix=PFX)
CORS(MOD_APP)

K8S = os.getenv('RUNNING_KUBERNETES', 'false')

SOLR_URL = os.getenv('SOLR_URL', 'http://sce-solr:8983')

# Define Controller(s)
@MOD_APP.route('/')
def index():
    """
    Index route
    :return:
    """
    return send_from_directory('static/pages', 'index.html')


@MOD_APP.route('/classify/createnew/<model>', methods=['GET'])
def create_new_model(model):
    """
    Create a new model
    :param model:
    :return:
    """
    # classifier.clear_model()
    classifier.new_model(model)
    return 'done'


@MOD_APP.route('/classify/listmodels', methods=['GET'])
def list_models():
    """
    List the models
    :return:
    """
    return jsonify(model_controller.get_models())


# POST Requests
@MOD_APP.route('/classify/update/<model>', methods=['POST'])
def build_model(model):
    """
    Build the model
    :param model:
    :return:
    """
    annotations = []
    data = request.get_json()
    for key in data:
        annotations.append(int(data[key]))
    # for item in data.split('&'):
    #    annotations.append(int(item.split('=')[1]))

    accuracy = classifier.update_model(model, annotations)
    return accuracy


@MOD_APP.route('/classify/upload/<model>', methods=['POST'])
def upload_model(model):
    """
    Upload Model
    :param model:
    :return:
    """
    return classifier.import_model(model)


@MOD_APP.route('/classify/download/<model>', methods=['GET'])
def download_model(model):
    """
    Download Model
    :param model:
    :return:
    """
    return classifier.export_model(model)


@MOD_APP.route('/classify/exist/<model>', methods=['POST'])
def check_model(model):
    """
    CHeck model
    :param model:
    :return:
    """
    return classifier.check_model(model)


@MOD_APP.route('/classify/stats/<model>', methods=['GET'])
def model_stats(model):
    """
    Check the Model
    :param model:
    :return:
    """
    return classifier.check_model(model)


@MOD_APP.route('/cmd/crawler/exist/<model>', methods=['POST'])
def check_crawl_exists(model):
    """
    Check The Crawl Exists
    :param model:
    :return:
    """
    print('We need to do something with the model:' +model)
    return requests.post('http://sparkler:6000/cmd/crawler/exist/').text


@MOD_APP.route('/cmd/crawler/settings/<model>', methods=['POST'])
def set_sparkler_config(model):
    """
    Set Sparkler Configuration
    :param model:
    :return:
    """
    content = request.json
    set_sparkler_options(model, content)
    return content


@MOD_APP.route('/cmd/crawler/settings/<model>', methods=['GET'])
def get_sparkler_config(model):
    """
    Get Sparkler Configuration
    :param model:
    :return:
    """
    content = get_sparkler_options(model).getStore()
    return json.dumps(content)


@MOD_APP.route('/cmd/crawler/crawl/<model>', methods=['POST'])
def start_crawl(model):
    """
    Start Crawl
    :param model:
    :return:
    """
    crawl_opts = request.json
    content = get_sparkler_options(model).getStore()

    cmd_params = ''
    if crawl_opts is not None:
        if 'iterations' in crawl_opts:
            cmd_params += '-i ' + str(crawl_opts['iterations'])

        if 'topgroups' in crawl_opts:
            cmd_params += ' -tg' + str(crawl_opts['topgroups'])

        if 'topn' in crawl_opts:
            cmd_params += ' -tn ' + str(crawl_opts['topn'])

    cmd = ['bash', '-c', "echo \'" + yaml.safe_dump(
        content) + "\' > /data/sparkler/conf/sparkler-default.yaml && "
                   '/data/sparkler/bin/sparkler.sh crawl -cdb '+SOLR_URL+'/solr/crawldb '
           '-id ' + model + ' ' + cmd_params]
    print(cmd)
    if K8S.lower() == 'true':
        file = open('/var/run/secrets/kubernetes.io/serviceaccount/token', 'r')
        token = ''
        if file.mode == 'r':
            token = file.read()

        requests.delete(
            'https://kubernetes.default.svc.cluster.local/api/v1/'
            'namespaces/default/pods/' + model + 'crawl',
            headers={'content-type': 'application/json',
                     'Authorization': 'Bearer ' + token}, verify=False)
        json_tpl = {'kind': 'Pod', 'apiVersion': 'v1',
                    'metadata': {'name': model + 'crawl',
                                 'labels': {'run': model + 'seed'}},
                    'spec': {
                        'containers': [
                            {'name': model + 'crawl',
                             'image': 'uscdatascience/sparkler:latest',
                             'command': cmd,
                             'resources': {}}], 'restartPolicy': 'Never',
                        'dnsPolicy': 'ClusterFirst'}, 'status': {}}
        requests.post('https://kubernetes.default.svc.cluster.local/'
                      'api/v1/namespaces/default/pods', json=json_tpl,
                      headers={'content-type': 'application/json',
                               'Authorization': 'Bearer ' + token}, verify=False)
        return 'crawl started'

    print('Removing old container')
    pcmd = ['docker', 'rm', model + 'crawl']
    subprocess.call(pcmd)
    print('Pulling latest')
    qcmd = ['docker', 'pull', 'uscdatascience/sparkler:latest']
    subprocess.Popen(qcmd)
    print('Running container')
    qcmd = ['docker', 'run', '--network', 'compose_default', '--name', model + 'crawl',
            'uscdatascience/sparkler:latest'] + cmd
    subprocess.Popen(qcmd)
    return 'crawl started'


@MOD_APP.route('/cmd/crawler/crawl/<model>', methods=['DELETE'])
def stop_crawl(model):
    """
    Stop Crawl
    :param model:
    :return:
    """
    if K8S.lower() == 'true':
        file = open('/var/run/secrets/kubernetes.io/serviceaccount/token', 'r')
        token = ''
        if file.mode == 'r':
            token = file.read()

        requests.delete(
            'https://kubernetes.default.svc.cluster.local/api/v1/'
            'namespaces/default/pods/' + model + 'crawl',
            headers={'content-type': 'application/json',
                     'Authorization': 'Bearer ' + token}, verify=False)

        return 'crawl ended'

    qcmd = ['docker', 'stop', model + 'crawl']
    subprocess.call(qcmd)
    return 'crawl ended'


@MOD_APP.route('/cmd/crawler/crawler/<model>', methods=['GET'])
def crawl_status(model):
    """
    Get the crawl status
    :param model:
    :return:
    """
    if K8S.lower() == 'true':
        file = open('/var/run/secrets/kubernetes.io/serviceaccount/token', 'r')
        token = ''
        if file.mode == 'r':
            token = file.read()
        response = requests.get('https://kubernetes.default.svc.cluster.local/'
                                'api/v1/namespaces/default/pods',
                                headers={'content-type': 'application/json',
                                         'Authorization': 'Bearer ' + token}, verify=False)
        return jsonify(response.json())

    qcmd = ['docker', 'container', 'ls', '--filter', 'name=' + model]
    output = subprocess.check_output(qcmd)

    if model in output.decode('utf-8'):
        return jsonify({'running': 'true'})
    return jsonify({'running': 'false'})


@MOD_APP.route('/cmd/crawler/int/<model>', methods=['POST'])
def kill_crawl_gracefully(model):
    """
    Graceful Crawl Kill
    :param model:
    :return:
    """
    if K8S.lower() == 'true':
        file = open('/var/run/secrets/kubernetes.io/serviceaccount/token', 'r')
        token = ''
        if file.mode == 'r':
            token = file.read()

        requests.delete(
            'https://kubernetes.default.svc.cluster.local/api/v1/namespaces/'
            'default/pods/' + model + 'crawl',
            headers={'content-type': 'application/json',
                     'Authorization': 'Bearer ' + token}, verify=False)

        return 'crawl killed'

    qcmd = ['docker', 'stop', model + 'crawl']
    subprocess.call(qcmd)
    return 'crawl killed'


@MOD_APP.route('/cmd/crawler/kill/<model>', methods=['POST'])
def force_kill_crawl(model):
    """
    Force kill the crawl
    :param model:
    :return:
    """
    if K8S.lower() == 'true':
        file = open('/var/run/secrets/kubernetes.io/serviceaccount/token', 'r')
        token = ''
        if file.mode == 'r':
            token = file.read()

        requests.delete(
            'https://kubernetes.default.svc.cluster.local/api/'
            'v1/namespaces/default/pods/' + model + 'crawl',
            headers={'content-type': 'application/json',
                     'Authorization': 'Bearer ' + token}, verify=False)

        return 'crawl killed'

    qcmd = ['docker', 'stop', model + 'crawl']
    subprocess.call(qcmd)
    return 'crawl killed'


@MOD_APP.route('/cmd/seed/upload/<model>', methods=['POST'])
def upload_seed(model):
    """
    Upload the Seeds
    :param model:
    :return:
    """
    print(request.get_data())
    new_full_list = update_seed_urls(model, request.get_data().splitlines())
    seeds = request.get_data().splitlines()
    urls = []
    for seed in seeds:
        urls.append(seed.decode('utf-8'))

    joined_urls = ' -su '.join(urls)
    cmd = ['/data/sparkler/bin/sparkler.sh', 'inject', '-cdb',
           SOLR_URL+'/solr/crawldb', '-su', joined_urls, '-id',
           model]
    if K8S.lower() == 'true':
        file = open('/var/run/secrets/kubernetes.io/serviceaccount/token', 'r')
        token = ''
        if file.mode == 'r':
            token = file.read()
        requests.delete('https://kubernetes.default.svc.cluster.local/api/'
                        'v1/namespaces/default/pods/' + model + 'seed',
                        headers={'content-type': 'application/json',
                                 'Authorization': 'Bearer ' + token}, verify=False)
        json_template = {'kind': 'Pod', 'apiVersion': 'v1',
                         'metadata': {'name': model + 'seed',
                                      'labels': {'run': model + 'seed'}}, 'spec': {
                                          'containers': [
                                              {'name': model + 'seed',
                                               'image': 'registry.gitlab.com/'
                                                        'sparkler-crawl-environment/'
                                                        'sparkler/sparkler:memex-dd',
                                               'command': cmd,
                                               'resources': {}}], 'restartPolicy': 'Never',
                                          'dnsPolicy': 'ClusterFirst'}, 'status': {}}
        requests.post('https://kubernetes.default.svc.cluster.local/api/v1/namespaces/default/pods'
                      , json=json_template,
                      headers={'content-type': 'application/json',
                               'Authorization': 'Bearer ' + token}, verify=False)
        return 'seed urls uploaded'

    pcmd = ['docker', 'rm', model + 'seed']
    qcmd = ['docker', 'run', '--network', 'compose_default', '--name', model + 'seed',
            'uscdatascience/sparkler:latest'] + cmd
    subprocess.call(pcmd)
    subprocess.Popen(qcmd)
    return json.dumps(new_full_list)


@MOD_APP.route('/cmd/seed/fetch/<model>', methods=['GET'])
def feeds(model):
    """
    Fetch Seeds
    :param model:
    :return:
    """
    seeds = fetch_seeds(model)
    if seeds is None:
        return ''

    return json.dumps(seeds)
