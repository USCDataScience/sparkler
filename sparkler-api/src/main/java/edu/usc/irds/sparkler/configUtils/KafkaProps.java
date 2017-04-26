package edu.usc.irds.sparkler.configUtils;


import edu.usc.irds.sparkler.BaseConfig;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.net.URI;

public class KafkaProps implements BaseConfig {
    @NotNull
    private boolean enable;
    @NotNull
    private URI listeners;
    @NotEmpty
    private String topic;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public URI getListeners() {
        return listeners;
    }

    public void setListeners(URI listeners) {
        this.listeners = listeners;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
