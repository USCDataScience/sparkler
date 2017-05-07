package edu.usc.irds.sparkler.configurationPropterites;


import org.hibernate.validator.constraints.NotEmpty;

public class SparkProperties {
    @NotEmpty(message = "spark.master needs to be provided")
    private String sparkMaster;

    public String getSparkMaster() {
        return sparkMaster;
    }

    public void setSparkMaster(String sparkMaster) {
        this.sparkMaster = sparkMaster;
    }
}
