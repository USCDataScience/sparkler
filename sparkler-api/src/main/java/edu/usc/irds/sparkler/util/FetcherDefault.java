package edu.usc.irds.sparkler.util;

import edu.usc.irds.sparkler.AbstractExtensionPoint;
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
import java.util.function.Function;

/**
 * This class is a default implementation of {@link Fetcher} contract.
 * This fetcher doesn't depends on external libraries,
 * instead it uses URLConnection provided by JDK to fetch the resources.
 *
 */
public class FetcherDefault extends AbstractExtensionPoint
        implements Fetcher, Function<Resource, FetchedData> {

    public static final Logger LOG = LoggerFactory.getLogger(FetcherDefault.class);
    public static final Integer FETCH_TIMEOUT = 1000;
    public static final Integer DEFAULT_ERROR_CODE = 400;

    @Override
    public Iterator<FetchedData> fetch(Iterator<Resource> resources) throws Exception {
        return new StreamTransformer<>(resources, this);
    }

    public FetchedData fetch(Resource resource) throws Exception {
        LOG.info("DEFAULT FETCHER {}", resource.url());
        URLConnection urlConn = new URL(resource.url()).openConnection();
        urlConn.setConnectTimeout(FETCH_TIMEOUT);
        int responseCode = ((HttpURLConnection)urlConn).getResponseCode();
        LOG.debug("STATUS CODE : " + responseCode + " " + resource.url());
        try (InputStream inStream = urlConn.getInputStream()) {
            byte[] rawData = IOUtils.toByteArray(inStream);
            FetchedData fetchedData = new FetchedData(rawData, urlConn.getContentType(), responseCode);
            resource.setStatus(ResourceStatus.FETCHED.toString());
            fetchedData.setResource(resource);
            return fetchedData;
        }
    }

    @Override
    public FetchedData apply(Resource resource) {
        try {
            return this.fetch(resource);
        }
        catch (Exception e) {
            LOG.warn("FETCH-ERROR {}", resource.url());
            LOG.debug(e.getMessage(), e);
            return this.fakeFetchedData(resource, ResourceStatus.ERROR);
        }
    }

    protected FetchedData fakeFetchedData(Resource r, ResourceStatus status) {
        FetchedData fetchedData = new FetchedData(new byte[0], "", DEFAULT_ERROR_CODE);
        r.setStatus(status.toString());
        fetchedData.setResource(r);
        return fetchedData;
    }
}
