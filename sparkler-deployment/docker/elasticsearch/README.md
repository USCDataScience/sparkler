# Start Sparkler with Elasticsearch
Please have docker-compose installed on the local machine. Instructions can be found [here.](https://docs.docker.com/compose/install/)

Start the Network with containers for Sparkler, Elasticsearch, Kibana:
Note: The Sparkler container will not include Solr as the backend is Elasticsearch.

Use the following script to manage the docker-compose network

```bash
cd sparkler-deployment/docker/elasticsearch
# Display all the options of the script
python dockler.py --help

# Start the network with three containers (Sparkler, Elasticsearch, Kibana)
python dockler.py --up

# Stop the network (later when you are done)
python dockler.py --down
```


Run a sample crawl
```bash
 docker run -v elastic:/elasticsearch-7.17.0/data ghcr.io/uscdatascience/sparkler/sparkler:main inject -id myid -su 'http://www.bbc.com/news'

 docker run -v elastic:/elasticsearch-7.17.0/data ghcr.io/uscdatascience/sparkler/sparkler:main crawl -id myid -tn 100 -i 2

```