package edu.usc.irds.sparkler.sparklerrest.solr;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;

public class SolrImpl {

    public HttpSolrClient getSolrClient(){


        final String solrUrl = "http://localhost:8983/solr";
        return new HttpSolrClient.Builder(solrUrl)
                .withConnectionTimeout(10000)
                .withSocketTimeout(60000)
                .build();


    }

    public JSONObject getSolrInfo() throws IOException, ParseException {
        HttpClient client = getSolrClient().getHttpClient();


        HttpGet request = new HttpGet("http://localhost:8983/solr/admin/info/system?_=1611276084265&wt=jso");
        request.addHeader("accept", "application/json");
        HttpResponse response = client.execute(request);

        String str = IOUtils.toString(response.getEntity().getContent());
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(str);

        return json;
    }
}
