package edu.usc.irds.sparkler.plugin.ddsvn;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * This class provides utility methods to perform the most essential functions that realize a request execution process
 * by relying on a @{@link HttpClient}.
 */
public class ApacheHttpRestClient {

    //TODO logger

    /**
     *
     */
    private CloseableHttpClient httpClient;

    // Create a custom response handler
    private ResponseHandler<String> responseHandler;

    public final static void main(String[] args) {

        ApacheHttpRestClient client = new ApacheHttpRestClient();

        try {
            client.httpGetRequest("http://localhost:5000/classify/predict/car");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            client.close();
        }
    }

    public ApacheHttpRestClient() {

        this.httpClient = HttpClients.createDefault();

        //TODO lambda
        this.responseHandler = new ResponseHandler<String>() {

            @Override
            public String handleResponse(
                    final HttpResponse response) throws IOException {
                int status = response.getStatusLine().getStatusCode();
                if (status >= HttpStatus.SC_OK && status < HttpStatus.SC_MULTIPLE_CHOICES) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                }
                else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            }

        };
    }

    public String httpGetRequest(String uriString) throws IOException {
        URI uri = URI.create(uriString);
        return this.httpGetRequest(uri);
    }

    public String httpGetRequest(String scheme, String host, String path, List<NameValuePair> parameters) throws IOException, URISyntaxException {
        URI uri = new URIBuilder()
                .setScheme(scheme)
                .setHost(host)
                .setPath(path)
                .setParameters(parameters)
                .build();
        return this.httpGetRequest(uri);
    }

    public String httpGetRequest(URI uri) throws IOException {
        HttpGet httpGet = new HttpGet(uri);

        //TODO logging
//        System.out.println("Executing request " + httpGet);

        String responseBody = httpClient.execute(httpGet, this.responseHandler);
        HttpEntity entity = httpClient.execute(httpGet).getEntity();

        //TODO logging
//        System.out.println("----------------------------------------");
//        System.out.println(responseBody);

        return responseBody;
    }

    public void close() {
        try {
            httpClient.close();
        } catch (IOException e) {
            //TODO
            e.printStackTrace();
        }
    }
}