package edu.usc.irds.sparkler.configUtils;


import edu.usc.irds.sparkler.BaseConfig;



public class UrlFilterProps implements BaseConfig {
    private String regexFile;

    public String getRegexFile() {
        return regexFile;
    }

    public void setRegexFile(String regexFile) {
        this.regexFile = regexFile;
    }
}
