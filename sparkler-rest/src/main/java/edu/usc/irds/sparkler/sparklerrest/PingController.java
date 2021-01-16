package edu.usc.irds.sparkler.sparklerrest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.Socket;

@RequestMapping("/pinger")
@Controller
public class PingController {

    @Value("${ping.hostname:127.0.0.1}")
    String hostname  = "127.0.0.1";

    Integer port = 8080;

    @Autowired(required = false)
    Socket socket = null;

    public ResponseEntity<String> pinger(){
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(CustomMediaType.TURBO_STREAM);

        return ResponseEntity.ok().contentType(CustomMediaType.TURBO_STREAM).body("<turbo-stream action=\"append\" target=\"pings\">\n" +
                "  <template>\n" +
                "    <li>70</li>\n" +
                "  </template>\n" +
                "</turbo-stream>");
    }

    private Long ping(Socket socket){
        return Long.valueOf(80);

    }
}
