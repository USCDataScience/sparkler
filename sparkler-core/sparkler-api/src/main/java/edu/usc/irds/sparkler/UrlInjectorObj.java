package edu.usc.irds.sparkler;

public class UrlInjectorObj {
    private String url;
    private String metadata;
    private String httpMethod;

    public UrlInjectorObj(String url, String metadata, String httpMethod){
        this.url = url;
        this.metadata = metadata;
        this.httpMethod = httpMethod;
    }


    public String getUrl(){
        return url;
    }

    public String getMetadata(){
        return metadata;
    }

    public String getHttpMethod(){
        return httpMethod;
    }
}
