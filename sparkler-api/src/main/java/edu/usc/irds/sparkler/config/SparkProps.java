package edu.usc.irds.sparkler.config;

import edu.usc.irds.sparkler.BaseConfig;
import org.hibernate.validator.constraints.NotEmpty;

public class SparkProps implements BaseConfig {
    @NotEmpty(message = "spark.url cannot be null")
    private String master;

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }
}
