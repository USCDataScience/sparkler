package edu.usc.irds.sparkler.configurationPropterites;


import javax.validation.constraints.NotNull;
import java.net.URI;

/**
 * @apiNote This class helps in making config variables for kafka
 * plugin in sparkler.
 */
public class KafkaProperties {
    @NotNull
    private boolean kafkaEnable;

    private String kafkaTopic;

    private URI kafkaListeners;

    //Getters and Setters

    public Boolean getKafkaEnable() {
        return kafkaEnable;
    }

    public void setKafkaEnable(Boolean kafkaEnable) {
        this.kafkaEnable = kafkaEnable;
    }

    public String getKafkaTopic() {
        return kafkaTopic;
    }

    public void setKafkaTopic(String kafkaTopic) {
        this.kafkaTopic = kafkaTopic;
    }

    public URI getKafkaListeners() {
        return kafkaListeners;
    }

    public void setKafkaListeners(URI kafkaListeners) {
        this.kafkaListeners = kafkaListeners;
    }

    @Override
    public String toString() {
        return ("Kafka Topic:" + this.kafkaTopic + " Kafka Listeners:" + this.kafkaListeners);
    }
}