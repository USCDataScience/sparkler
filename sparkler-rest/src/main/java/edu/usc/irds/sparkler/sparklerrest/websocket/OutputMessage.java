package edu.usc.irds.sparkler.sparklerrest.websocket;


public class OutputMessage {
    String from;
    String text;
    String time;

    public OutputMessage(String from, String text, String time) {

        this.from = from;
        this.text = text;
        this.time = time;

    }
}
