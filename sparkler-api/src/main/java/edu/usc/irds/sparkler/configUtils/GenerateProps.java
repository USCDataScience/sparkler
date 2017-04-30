package edu.usc.irds.sparkler.configUtils;


import edu.usc.irds.sparkler.BaseConfig;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class GenerateProps implements BaseConfig {
    @NotNull(message = "generate.topN cannot be null")
    @Min(value = 1, message = "generate.topN value cannot be less than 1")
    private int topN;
    @NotNull(message = "generate.topGroups cannot be null")
    @Min(value = 1, message = "generate.topGroups value cannot be less than 1")
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
