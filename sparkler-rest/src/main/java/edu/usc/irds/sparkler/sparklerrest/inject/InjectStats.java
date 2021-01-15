package edu.usc.irds.sparkler.sparklerrest.inject;

public class InjectStats {

    private Integer numberOfUrls;
    private String[] listOfUrls;
    private String jobId;
    private String createdAt;

    public InjectStats() {
    }

    public Integer getNumberOfUrls() {
        return numberOfUrls;
    }

    public void setNumberOfUrls(Integer numberOfUrls) {
        this.numberOfUrls = numberOfUrls;
    }

    public String[] getListOfUrls() {
        return listOfUrls;
    }

    public void setListOfUrls(String[] listOfUrls) {
        this.listOfUrls = listOfUrls;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
