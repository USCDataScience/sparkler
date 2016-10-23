package edu.usc.irds.sparkler.util;

import edu.usc.irds.sparkler.AbstractExtensionPoint;
import edu.usc.irds.sparkler.CloseableIterator;
import edu.usc.irds.sparkler.Fetcher;
import edu.usc.irds.sparkler.model.FetchedData;
import edu.usc.irds.sparkler.model.Resource;
import edu.usc.irds.sparkler.model.ResourceStatus;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;

/**
 * Created by karanjeetsingh on 10/23/16.
 */
public class FetcherDefault extends AbstractExtensionPoint implements Fetcher {

    public static final Logger LOG = LoggerFactory.getLogger(FetcherDefault.class);
    public static final Integer FETCH_TIMEOUT = 1000;
    public static final Integer DEFAULT_ERROR_CODE = 400;

    @Override
    public Iterator<FetchedData> fetch(Iterator<Resource> resources) throws Exception {
        try ( CloseableIterator<FetchedData> iterator = new FetchIterator(resources) ) {
            return iterator;
        }
    }

    public FetchedData fetch(Resource resource) {
        LOG.info("DEFAULT FETCHER {}", resource.getUrl());
        Integer responseCode = DEFAULT_ERROR_CODE;
        FetchedData fetchedData;
        try {
            URLConnection urlConn = new URL(resource.getUrl()).openConnection();
            urlConn.setConnectTimeout(FETCH_TIMEOUT);
            responseCode = ((HttpURLConnection)urlConn).getResponseCode();
            LOG.debug("STATUS CODE : " + responseCode + " " + resource.getUrl());

            InputStream inStream = urlConn.getInputStream();
            byte[] rawData = IOUtils.toByteArray(inStream);
            inStream.close();
            fetchedData = new FetchedData(rawData, urlConn.getContentType(), responseCode);
            resource.setStatus(ResourceStatus.FETCHED.toString());
        } catch (Exception e) {
            LOG.warn("FETCH-ERROR {}", resource.getUrl());
            //e.printStackTrace()
            LOG.debug(e.getMessage(), e);
            fetchedData = new FetchedData(new byte[0], "", responseCode);
            resource.setStatus(ResourceStatus.ERROR.toString());
        }
        fetchedData.setResource(resource);
        return fetchedData;
    }

    public void closeResources() throws Exception {
    }

    public class FetchIterator implements CloseableIterator<FetchedData> {

        private Iterator<Resource> resources;

        public FetchIterator(Iterator<Resource> resources) {
            super();
            this.resources = resources;
        }

        @Override
        public boolean hasNext() {
            return resources.hasNext();
        }

        @Override
        public FetchedData next() {
            Resource resource = resources.next();
            return fetch(resource);
        }

        @Override
        public void close() throws Exception {
            if(!resources.hasNext()) {
                closeResources();
            }
        }
    }
}
