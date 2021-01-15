package edu.usc.irds.sparkler.sparklerrest;

public class Crawl {
    private final String userName;
    private final String messageContent;

    public Crawl(String userName, String messageContent) {
        this.userName = userName;
        this.messageContent = messageContent;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public String getUserName() {
        return userName;
    }
}
