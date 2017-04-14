package edu.usc.irds.sparkler.configUtils;

import edu.usc.irds.sparkler.BaseConfig;

public class SparkProps implements BaseConfig {
    private String master;

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }
}
