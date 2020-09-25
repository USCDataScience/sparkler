FROM uscdatascience/sce-domain-discovery:latest

RUN echo 'hello'

FROM ubuntu:bionic

WORKDIR /usr/src/app

RUN apt update && apt-get install -y sudo python3 python3-dev python3-pip docker.io apache2 libapache2-mod-wsgi-py3 curl && curl -sL https://deb.nodesource.com/setup_10.x | bash - && apt update && apt install -y nodejs

#RUN adduser --disabled-password --gecos '' docker
RUN adduser www-data sudo

RUN echo '%sudo ALL=(ALL) NOPASSWD:ALL' >> /etc/sudoers
COPY package*.json ./
COPY scripts/run.sh /
RUN npm ci --only=production
COPY . .
RUN  npm run build && apt update && chmod +x /run.sh && a2enmod proxy && a2enmod proxy_http && mkdir /var/www/html/explorer && cp -rf build/* /var/www/html/explorer/
COPY scripts/000-default.conf /etc/apache2/sites-available/

EXPOSE 8080
EXPOSE 80

COPY --from=0 /projects/sce-domain-discovery /sce

RUN cd /sce/webui && pip3 install -r requirements.txt && mkdir /images && mkdir /models && chown www-data:www-data /images && chown www-data:www-data /models && gpasswd -a www-data docker
RUN ln -sf /dev/stdout /var/log/apache2/access.log \
    && ln -sf /dev/stderr /var/log/apache2/error.log
CMD [ "/run.sh" ]
