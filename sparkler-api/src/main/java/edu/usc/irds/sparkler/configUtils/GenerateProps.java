package edu.usc.irds.sparkler.configUtils;


import edu.usc.irds.sparkler.BaseConfig;

public class GenerateProps implements BaseConfig {
    private int topN;
    private int topGroups;

    public int getTopN() {
        return topN;
    }

    public void setTopN(int topN) {
        this.topN = topN;
    }

    public int getTopGroups() {
        return topGroups;
    }

    public void setTopGroups(int topGroups) {
        this.topGroups = topGroups;
    }
}
