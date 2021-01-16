package edu.usc.irds.sparkler.sparklerrest;

import edu.usc.irds.sparkler.sparklerrest.websocket.Message;
import edu.usc.irds.sparkler.sparklerrest.websocket.OutputMessage;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;

import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@CrossOrigin("http://localhost:3000")
public class SparklerController {

    @MessageMapping("/chat")
    @SendTo("/chat")
    public OutputMessage index() {
        String time = new SimpleDateFormat("HH:mm").format(new Date());
        return new OutputMessage("from tom","Why hello there", time);
    }

}
