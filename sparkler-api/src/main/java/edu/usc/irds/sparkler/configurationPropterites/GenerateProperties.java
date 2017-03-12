package edu.usc.irds.sparkler.configurationPropterites;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class GenerateProperties {
    @NotNull(message = "generate.topn needs to be provided")
    @Min(value = 1, message = "generate.topn should be at least 1")
    private int generateTopn;

    @NotNull(message = "generate.top.groups needs to be provided")
    @Min(value = 1, message = "generate.top.groups should be at least 1")
    private int generateTopGroups;

    public int getGenerateTopn() {
        return generateTopn;
    }

    public void setGenerateTopn(int generateTopn) {
        this.generateTopn = generateTopn;
    }

    public int getGenerateTopGroups() {
        return generateTopGroups;
    }

    public void setGenerateTopGroups(int generateTopGroups) {
        this.generateTopGroups = generateTopGroups;
    }
}
