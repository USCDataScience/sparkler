package edu.usc.irds.sparkler.util;

import edu.usc.irds.sparkler.AbstractExtensionPoint;
import edu.usc.irds.sparkler.Constants;
import edu.usc.irds.sparkler.Fetcher;
import edu.usc.irds.sparkler.JobContext;
import edu.usc.irds.sparkler.SparklerException;
import edu.usc.irds.sparkler.config.FetcherProps;
import edu.usc.irds.sparkler.config.SparklerConfig;
import edu.usc.irds.sparkler.model.FetchedData;
import edu.usc.irds.sparkler.model.Resource;
import edu.usc.irds.sparkler.model.ResourceStatus;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    public static final Integer CONTENT_LIMIT = 100 * 1024 * 1024;
    public static final String TRUNCATED = "X-Content-Truncated";

    protected List<String> userAgents;
    protected int userAgentIndex = 0; // index for rotating the agents
    protected Map<String, String> httpHeaders;

    @Override
    public void init(JobContext context, String pluginId) throws SparklerException {
        super.init(context, pluginId);
        SparklerConfig conf = context.getConfiguration();
        FetcherProps fetcherConf = conf.getFetcher();
        if (fetcherConf.getUserAgents() != null) {
            this.userAgents = fetcherConf.getUserAgents();
        } else {
            this.userAgents = Collections.EMPTY_LIST;
        }

        this.httpHeaders = fetcherConf.getHeaders();

    }

    /**
     * Gets a user agent from a list of configured values, rotates the list for each call
     * @return get a user agent string from the list of configured values
     */
    public String getUserAgent(){
        if (userAgents == null || userAgents.isEmpty()){
            return null;
        }
        String agent = userAgents.get(userAgentIndex);
        synchronized (this) { // rotate index
            userAgentIndex = (userAgentIndex + 1) % userAgents.size();
        }
        return agent;
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
            LOG.debug("Adding headers:{}", httpHeaders.keySet());
        } else {
            LOG.debug("No headers are available");
        }
        String userAgentValue = getUserAgent();
        if (userAgentValue != null) {
            LOG.debug(USER_AGENT + ": " + userAgentValue);
            urlConn.setRequestProperty(USER_AGENT, userAgentValue);
        } else {
            LOG.debug("No rotating agents are available");
        }

        urlConn.setConnectTimeout(CONNECT_TIMEOUT);
        urlConn.setReadTimeout(READ_TIMEOUT);
        int responseCode = ((HttpURLConnection)urlConn).getResponseCode();
        LOG.debug("STATUS CODE : " + responseCode + " " + resource.getUrl());
        try (InputStream inStream = urlConn.getInputStream()) {
            byte[] rawData;
            if (inStream != null) {
                rawData = IOUtils.toByteArray(inStream);
            } else {
                rawData = new byte[0]; //no content received
            }
            FetchedData fetchedData = new FetchedData(rawData, urlConn.getContentType(), responseCode);
            resource.setStatus(ResourceStatus.FETCHED.toString());
            fetchedData.setResource(resource);
            fetchedData.setHeaders(urlConn.getHeaderFields());
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
