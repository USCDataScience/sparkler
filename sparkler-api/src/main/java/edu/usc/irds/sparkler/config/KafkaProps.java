package edu.usc.irds.sparkler.config;


import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.net.URL;

public class KafkaProps implements BaseConfig {
    @NotNull(message = "kafka.enable cannot be null")
    private boolean enable;
    @NotNull(message = "kafka.listeners cannot be null")
    private URL listeners;
    @NotEmpty(message = "kafka.topic cannot be empty")
    private String topic;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public URL getListeners() {
        return listeners;
    }

    public void setListeners(URL listeners) {
        this.listeners = listeners;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
