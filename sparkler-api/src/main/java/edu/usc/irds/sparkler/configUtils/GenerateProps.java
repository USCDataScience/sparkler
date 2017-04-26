package edu.usc.irds.sparkler.configUtils;


import com.sun.istack.NotNull;
import edu.usc.irds.sparkler.BaseConfig;

import javax.validation.constraints.Min;

public class GenerateProps implements BaseConfig {
    @NotNull
    @Min(1)
    private int topN;
    @NotNull
    @Min(1)
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
