package edu.usc.irds.sparkler.util;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import edu.usc.irds.sparkler.*;
import edu.usc.irds.sparkler.model.FetchedData;
import edu.usc.irds.sparkler.model.Resource;
import edu.usc.irds.sparkler.model.ResourceStatus;
import org.apache.commons.io.IOUtils;
import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
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
    public static final Logger LOG = new LoggerContext().getLogger(FetcherDefault.class);
    public static final Integer CONNECT_TIMEOUT = 5000; // Milliseconds. FIXME: Get from Configuration
    public static final Integer READ_TIMEOUT = 10000; // Milliseconds. FIXME: Get from Configuration
    public static final Integer CONTENT_LIMIT = 100 * 1024 * 1024; // Bytes. FIXME: Get from Configuration
    public static final Integer DEFAULT_ERROR_CODE = 400;
    public static final String USER_AGENT = "User-Agent";
    public static final String TRUNCATED = "X-Content-Truncated";

    protected List<String> userAgents;
    protected int userAgentIndex = 0; // index for rotating the agents
    protected Map<String, String> httpHeaders;

    public FetcherDefault(){}

    String proxyurl = null;
    Integer proxyport = null;
    String proxyscheme = null;
    @Override
    public void init(JobContext context, String pluginId) throws SparklerException {
        super.init(context, pluginId);
        SparklerConfiguration conf = context.getConfiguration();
        if (conf.containsKey("fetcher.proxy.url")){
            proxyurl = (String) conf.get("fetcher.proxy.url");
        }
        if (conf.containsKey("fetcher.proxy.port")){
            proxyport = Integer.parseInt((String) conf.get("fetcher.proxy.port"));
        }
        if (conf.containsKey("fetcher.proxy.scheme")){
            proxyscheme = (String) conf.get("fetcher.proxy.scheme");
        }
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
        HttpClientBuilder httpClient = HttpClients.custom();
        RequestConfig.Builder requestConfig = RequestConfig.custom();
        if(proxyurl != null && !proxyurl.equals("")){
            LOG.info("Setting up the Proxy: " + proxyurl + " " + proxyport + " " + proxyscheme);
            HttpHost proxy;
            if(proxyscheme == null) {
                 proxy = new HttpHost(proxyurl, proxyport);
            } else{
                proxy = new HttpHost(proxyurl, proxyport, proxyscheme);
            }
            DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);
            httpClient.setRoutePlanner(routePlanner);
            requestConfig.setProxy(proxy);
        }

        if (httpHeaders != null){
            List<Header> headers = new ArrayList<>();
            for (Map.Entry<String, String> entry : httpHeaders.entrySet()) {
                Header header = new BasicHeader(entry.getKey(), entry.getValue());
                headers.add(header);
            }
            httpClient.setDefaultHeaders(headers);
            LOG.debug("Adding headers:{}", httpHeaders.keySet());
        } else {
            LOG.debug("No headers are available");
        }
        String userAgentValue = getUserAgent();

        if (userAgentValue != null) {
            LOG.debug(USER_AGENT + ": " + userAgentValue);
            httpClient.setUserAgent(userAgentValue);
        } else {
            LOG.debug("No rotating agents are available");
        }

        requestConfig.setConnectTimeout(CONNECT_TIMEOUT);
        requestConfig.setConnectionRequestTimeout(READ_TIMEOUT);
        requestConfig.setSocketTimeout(CONNECT_TIMEOUT);
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(30000).build());
        httpClient.setConnectionManager(cm);
        HttpRequestBase http = null;
        if(resource.getHttpMethod().equalsIgnoreCase("GET")){
            // TODO FIX FOR NON PROXY
            http = new HttpGet(resource.getUrl());
        } else if(resource.getHttpMethod().equalsIgnoreCase("POST")){
            // TODO FIX FOR NON PROXY
            http = new HttpPost(resource.getUrl());
        }
        if(resource.getMetadata()!=null && !resource.getMetadata().equals("")){
            JSONObject json = processMetadata(resource.getMetadata());

            if (json.containsKey("form")) {
                UrlEncodedFormEntity postData = processForm((JSONObject) json.get("form"));
                ((HttpPost)http).setEntity(postData);
            } else if (json.containsKey("JSON")) {
                String postData = processJson((JSONObject) json.get("JSON"));
                HttpEntity stringEntity = new StringEntity(postData, ContentType.APPLICATION_JSON);
                ((HttpPost)http).setEntity(stringEntity);
            }
        }

        RequestConfig r = requestConfig.build();
        CloseableHttpResponse response2 = httpClient.setDefaultRequestConfig(r).build().execute(http);
        int responseCode = response2.getStatusLine().getStatusCode();
        LOG.debug("STATUS CODE : " + responseCode + " " + resource.getUrl());
        boolean truncated = false;
        HttpEntity entity = response2.getEntity();
        InputStream is = entity.getContent();
        byte[] rawData = IOUtils.toByteArray(is);
        ByteArrayOutputStream bufferOutStream = new ByteArrayOutputStream();
        /*byte[] buffer = new byte[4096]; // 4kb buffer
        int read;
        while((read = inStream.read(buffer, 0, buffer.length)) != -1) {
            bufferOutStream.write(buffer, 0, read);
            if (bufferOutStream.size() >= CONTENT_LIMIT) {
                truncated = true;
                LOG.info("Content Truncated: {}, TotalSize={}, TruncatedSize={}", resource.getUrl(),
                        entity.getContentLength(), bufferOutStream.size());
                break;
            }
        }*/
        //bufferOutStream.flush();
        //byte[] rawData = bufferOutStream.toByteArray();


        String contentHash = null;
        if(rawData.length>0) {
            byte[] md5hash = MessageDigest.getInstance("MD5").digest(rawData);
            contentHash = toHexString(md5hash);
            resource.setContentHash(contentHash);

        }

        IOUtils.closeQuietly(bufferOutStream);
        String ctype = null;
        if(entity.getContentType() != null){
            ctype = entity.getContentType().getValue();
        }

        FetchedData fetchedData = new FetchedData(rawData, ctype, responseCode);
        resource.setStatus(ResourceStatus.FETCHED.toString());
        fetchedData.setResource(resource);
        Header[] headers = response2.getAllHeaders();
        Map<String, List<String>> headermap = new HashMap<>();
        for (Header h : headers){
            String name = h.getName();
            String val = h.getValue();
            List<String> l = new ArrayList<>();
            l.add(val);
            headermap.put(name, l);
        }
        fetchedData.setHeaders(headermap);
        fetchedData.setContenthash(contentHash);
        if (truncated) {
            fetchedData.getHeaders().put(TRUNCATED, Collections.singletonList(Boolean.TRUE.toString()));
        }

        response2.close();
        return fetchedData;

    }
    private static String toHexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();

        for (byte aByte : bytes) {
            String hex = Integer.toHexString(0xFF & aByte);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString();
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
            LOG.warn(e.getMessage(), e);
            FetchedData fetchedData = new FetchedData(new byte[0], "", statusCode);
            resource.setStatus(ResourceStatus.ERROR.toString());
            fetchedData.setResource(resource);
            return fetchedData;
        }
    }

    private String processJson(JSONObject object) {
        String s = object.toJSONString();
        return object.toJSONString();
    }

    private UrlEncodedFormEntity processForm(JSONObject object) {
        List<NameValuePair> form = new ArrayList<>();
        Set keys = object.keySet();
        for (Object o : keys) {
            String key = (String) o;
            String val = (String) object.get(key);
            form.add(new BasicNameValuePair(key, val));
        }
        return new UrlEncodedFormEntity(form, Consts.UTF_8);
    }

    private JSONObject processMetadata(String metadata) {
        if(metadata != null){
            JSONParser parser = new JSONParser();
            JSONObject json;
            try {
                json = (JSONObject) parser.parse(metadata);
                return json;

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return null;

    }
}
