package edu.usc.irds.sparkler.util;

import edu.usc.irds.sparkler.AbstractExtensionPoint;
import edu.usc.irds.sparkler.Constants;
import edu.usc.irds.sparkler.Fetcher;
import edu.usc.irds.sparkler.JobContext;
import edu.usc.irds.sparkler.SparklerConfiguration;
import edu.usc.irds.sparkler.SparklerException;
import edu.usc.irds.sparkler.model.FetchedData;
import edu.usc.irds.sparkler.model.Resource;
import edu.usc.irds.sparkler.model.ResourceStatus;
import org.apache.commons.io.IOUtils;
import org.pf4j.Extension;
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
public class FetcherDefault extends AbstractExtensionPoint implements Fetcher, Function<Resource, FetchedData> {
    //TODO: move this to a plugin named fetcher-default
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

    public FetcherDefault(){}

    @Override
    public void init(JobContext context, String pluginId) throws SparklerException {
        super.init(context, pluginId);
        SparklerConfiguration conf = context.getConfiguration();
        if (conf.containsKey(Constants.key.FETCHER_USER_AGENTS)) {
            Object agents = conf.get(Constants.key.FETCHER_USER_AGENTS);
            if (agents instanceof String) { // it is a file name
                try (InputStream stream = getClass().getClassLoader()
                        .getResourceAsStream(agents.toString())) {
                    if (stream == null) {
                        this.userAgents = Collections.EMPTY_LIST;
                        LOG.warn("Could not find Rotating user agents file in class path");
                    } else {
                        this.userAgents = IOUtils.readLines(stream, StandardCharsets.UTF_8).stream()
                                .map(String::trim)                               // white spaces are trimmed
                                .filter(s -> !s.isEmpty() && s.charAt(0) != '#') //remove empty and comment lines
                                .collect(Collectors.toList());
                    }
                }  catch (IOException e){
                    throw new SparklerException("Cant read user agent file", e);
                }
            } else { //it is a list
                this.userAgents = (List<String>) conf.get(Constants.key.FETCHER_USER_AGENTS);
            }
            //remove duplicates while preserving the order
            this.userAgents = new ArrayList<>(new LinkedHashSet<>(this.userAgents));
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
        long startTime = System.currentTimeMillis();
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

            FetchedData fetchedData = new FetchedData(rawData, urlConn.getContentType(), responseCode, System.currentTimeMillis() - startTime);
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
            FetchedData fetchedData = new FetchedData(new byte[0], "", statusCode, 0);
            resource.setStatus(ResourceStatus.ERROR.toString());
            fetchedData.setResource(resource);
            return fetchedData;
        }
    }
}
