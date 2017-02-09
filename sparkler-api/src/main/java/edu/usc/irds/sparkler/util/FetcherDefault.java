package edu.usc.irds.sparkler.util;

import edu.usc.irds.sparkler.*;
import edu.usc.irds.sparkler.model.FetchedData;
import edu.usc.irds.sparkler.model.Resource;
import edu.usc.irds.sparkler.model.ResourceStatus;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
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
    public static final Integer CONNECT_TIMEOUT = 5000;
    public static final Integer READ_TIMEOUT = 10000;
    public static final Integer DEFAULT_ERROR_CODE = 400;
    public static final String USER_AGENT = "User-Agent";

    protected List<String> userAgents;
    protected int userAgentIndex = 0; // index for rotating the agents
    protected Map<String, String> httpHeaders;


    @Override
    public void init(JobContext context) throws SparklerException {
        super.init(context);
        SparklerConfiguration conf = context.getConfiguration();
        if (conf.containsKey(Constants.key.FETCHER_USER_AGENTS)) {
            this.userAgents = (List<String>) conf.get(Constants.key.FETCHER_USER_AGENTS);
        }
        if (conf.containsKey(Constants.key.FETCHER_HEADERS)) {
            this.httpHeaders = (Map<String, String>) conf.get(Constants.key.FETCHER_HEADERS);
        }
    }

    /**
     * Gets a user agent from a list of configured values, rotates the list for each call
     * @return get a user agent string from the list of configured values
     */
    public String getUserAgent(){
        if (userAgents == null || userAgents.isEmpty()){
            return null;
        }
        userAgentIndex = (userAgentIndex + 1) % userAgents.size();
        return userAgents.get(userAgentIndex);
    }

    @Override
    public Iterator<FetchedData> fetch(Iterator<Resource> resources) throws Exception {
        return new StreamTransformer<>(resources, this);
    }

    public FetchedData fetch(Resource resource) throws Exception {
        LOG.info("DEFAULT FETCHER {}", resource.getUrl());
        URLConnection urlConn = new URL(resource.getUrl()).openConnection();
        if (httpHeaders != null){
            httpHeaders.entrySet().forEach(e -> urlConn.setRequestProperty(e.getKey(), e.getValue()));
        }
        String userAgentValue = getUserAgent();
        if (getUserAgent() != null) {
            LOG.debug(USER_AGENT + ": " + userAgentValue);
            urlConn.setRequestProperty(USER_AGENT, userAgentValue);
        }

        urlConn.setConnectTimeout(CONNECT_TIMEOUT);
        urlConn.setReadTimeout(READ_TIMEOUT);
        int responseCode = ((HttpURLConnection)urlConn).getResponseCode();
        LOG.debug("STATUS CODE : " + responseCode + " " + resource.getUrl());
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
        } catch (Exception e) {
            int statusCode =  DEFAULT_ERROR_CODE;
            if (e instanceof FileNotFoundException){
                statusCode = 404;
            }
            LOG.warn("FETCH-ERROR {}", resource.getUrl());
            LOG.debug(e.getMessage(), e);
            FetchedData fetchedData = new FetchedData(new byte[0], "", statusCode);
            resource.setStatus(ResourceStatus.ERROR.toString());
            fetchedData.setResource(resource);
            return fetchedData;
        }
    }

}
