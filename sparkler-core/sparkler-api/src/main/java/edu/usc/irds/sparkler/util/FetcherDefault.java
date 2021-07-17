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
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
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
    public static final Logger LOG = LoggerFactory.getLogger(FetcherDefault.class);
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
        HttpURLConnection urlConn = (HttpURLConnection) new URL(resource.getUrl()).openConnection();
        if (httpHeaders != null){
            httpHeaders.forEach(urlConn::setRequestProperty);
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
        urlConn.setRequestMethod(resource.getHttpMethod());
        if(resource.getMetadata()!=null && !resource.getMetadata().equals("")){
            JSONObject json = processMetadata(resource.getMetadata());
    
            if (json.containsKey("form")) {
                urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                urlConn.setRequestProperty( "charset", "utf-8");
                byte[] postData = processForm((JSONObject) json.get("form"));
                urlConn.setRequestProperty( "Content-Length", Integer.toString( postData.length ));    
                urlConn.setDoOutput(true);
                try( DataOutputStream wr = new DataOutputStream( urlConn.getOutputStream())) {
                    wr.write( postData );
                 }
            } else if (json.containsKey("JSON")) {
                //processJson((JSONObject) json.get("json"), connection);
                urlConn.setRequestProperty("Content-Type", "application/json");
                urlConn.setRequestProperty( "charset", "utf-8");
                byte[] postData = processJson((JSONObject) json.get("JSON"), null);
                urlConn.setRequestProperty( "Content-Length", Integer.toString( postData.length ));
                urlConn.setDoOutput(true);
                try( DataOutputStream wr = new DataOutputStream( urlConn.getOutputStream())) {
                    wr.write( postData );
                }
            }
        }
        int responseCode = ((HttpURLConnection)urlConn).getResponseCode();
        LOG.debug("STATUS CODE : " + responseCode + " " + resource.getUrl());
        boolean truncated = false;
        try (InputStream inStream = urlConn.getInputStream()) {
            ByteArrayOutputStream bufferOutStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096]; // 4kb buffer
            int read;
            while((read = inStream.read(buffer, 0, buffer.length)) != -1) {
                bufferOutStream.write(buffer, 0, read);
                if (bufferOutStream.size() >= CONTENT_LIMIT) {
                    truncated = true;
                    LOG.info("Content Truncated: {}, TotalSize={}, TruncatedSize={}", resource.getUrl(),
                            urlConn.getContentLength(), bufferOutStream.size());
                    break;
                }
            }
            bufferOutStream.flush();
            byte[] rawData = bufferOutStream.toByteArray();
            String contentHash = null;
            if(rawData.length>0) {
                byte[] md5hash = MessageDigest.getInstance("MD5").digest(rawData);
                contentHash = Base64.getEncoder().encodeToString(md5hash);
                resource.setContentHash(contentHash);
                if (jobContext.getConfiguration().containsKey("fetcher.persist.content.location")) {
                    URI uri = new URI(resource.getUrl());
                    String domain = uri.getHost();
                    File outputDirectory = Paths.get(jobContext.getConfiguration().get("fetcher.persist.content.location").toString(), jobContext.getId(), domain).toFile();
                    File outputFile;
                    if (jobContext.getConfiguration().get("fetcher.persist.content.filename").toString().equals("hash")) {
                        String ext = FilenameUtils.getExtension(resource.getUrl());
                        outputFile = Paths.get(jobContext.getConfiguration().get("fetcher.persist.content.location").toString(), jobContext.getId(), domain, contentHash + "." + ext).toFile();
                    } else {
                        outputFile = Paths.get(jobContext.getConfiguration().get("fetcher.persist.content.location").toString(), jobContext.getId(), domain, FilenameUtils.getName(resource.getUrl())).toFile();
                    }
                    outputDirectory.mkdirs();
                    try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                        outputStream.write(rawData);
                    }
                }
            }

            IOUtils.closeQuietly(bufferOutStream);
            FetchedData fetchedData = new FetchedData(rawData, urlConn.getContentType(), responseCode);
            resource.setStatus(ResourceStatus.FETCHED.toString());
            fetchedData.setResource(resource);
            fetchedData.setHeaders(urlConn.getHeaderFields());
            fetchedData.setContenthash(contentHash);
            if (truncated) {
                fetchedData.getHeaders().put(TRUNCATED, Collections.singletonList(Boolean.TRUE.toString()));
            }
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

    private byte[] processJson(JSONObject object, HttpURLConnection conn) {

        JSONParser parser = new JSONParser();
        try {
            //JSONObject json = (JSONObject) parser.parse(object.toString());
            String s = object.toJSONString();
            System.out.println("PROCESSED JSON: "+ s);
            return object.toJSONString().getBytes("UTF-8");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }

    private byte[] processForm(JSONObject object) {
        Set keys = object.keySet();
        Iterator keyIter = keys.iterator();
        String content = "";
        for (int i = 0; keyIter.hasNext(); i++) {
            Object key = keyIter.next();
            if (i != 0) {
                content += "&";
            }
            try {
                content += key + "=" + URLEncoder.encode((String) object.get(key), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return content.getBytes(StandardCharsets.UTF_8);
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
