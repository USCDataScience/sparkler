FROM node:10

WORKDIR /usr/src/app

COPY package*.json ./
COPY scripts/run.sh /
RUN npm ci --only=production
COPY . .
RUN  npm run build && apt update && apt install -y apache2 && chmod +x /run.sh && a2enmod proxy && a2enmod proxy_http && mkdir /var/www/html/explorer && cp -rf build/* /var/www/html/explorer/
COPY scripts/000-default.conf /etc/apache2/sites-available/

EXPOSE 8080
EXPOSE 80

CMD [ "/run.sh" ]
