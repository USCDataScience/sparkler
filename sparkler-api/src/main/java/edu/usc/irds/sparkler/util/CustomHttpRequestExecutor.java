package edu.usc.irds.sparkler.util;

import org.apache.http.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestExecutor;
import java.io.IOException;

public class CustomHttpRequestExecutor extends HttpRequestExecutor {
    private final String proxyIpAttributeId = "proxy:ip";

    @Override   public void preProcess(HttpRequest request, HttpProcessor processor, HttpContext context) throws IOException, HttpException {
        HttpClientContext clientContext = HttpClientContext.adapt(context);
        String proxyIp = clientContext.getAttribute(proxyIpAttributeId, String.class);

        // used for HTTPS requests
        if(proxyIp != null && "CONNECT".equalsIgnoreCase(request.getRequestLine().getMethod())) {
            request.setHeader("X-ProxyMesh-IP", proxyIp);
        }

        super.preProcess(request, processor, context);
    }


    @Override protected HttpResponse doSendRequest(HttpRequest request, HttpClientConnection conn, HttpContext context) throws IOException, HttpException {
        HttpClientContext clientContext = HttpClientContext.adapt(context);
        String proxyIp = clientContext.getAttribute(proxyIpAttributeId, String.class);
        // used for HTTP requests
        boolean isHttps = true; // set something on your context to see if https was used
        if(!isHttps && proxyIp != null) {
            request.setHeader("X-ProxyMesh-IP", proxyIp);
        }
        return super.doSendRequest(request, conn, context);
    }

    @Override protected HttpResponse doReceiveResponse(HttpRequest request, HttpClientConnection conn, HttpContext context) throws HttpException, IOException {
        HttpClientContext clientContext = HttpClientContext.adapt(context);
        HttpResponse response = super.doReceiveResponse(request, conn, context);
        Header proxyIpNotFoundHeader = response.getFirstHeader("X-ProxyMesh-IP-Not-Found");
        Header proxyIpHeader = response.getFirstHeader("X-ProxyMesh-IP");

        if(proxyIpNotFoundHeader != null) {
            System.err.println("Proxy IP not found!");
        } else if(proxyIpHeader != null) {
            String proxyIp = proxyIpHeader.getValue();
            clientContext.setAttribute(proxyIpAttributeId, proxyIp);
        }
        return response;
    }
}
