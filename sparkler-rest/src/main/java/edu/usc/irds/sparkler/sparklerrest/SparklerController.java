package edu.usc.irds.sparkler.sparklerrest;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;

@RestController
@CrossOrigin("http://localhost:3000")
public class SparklerController {

    @MessageMapping("/chat")
    @SendTo("/chat")
    public Crawl index(Crawl message) {
        return message;
    }

}
