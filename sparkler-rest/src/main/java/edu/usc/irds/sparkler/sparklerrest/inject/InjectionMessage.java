package edu.usc.irds.sparkler.sparklerrest.inject;

public class InjectionMessage {
    String jobId;
    String error;
    String message;

    public InjectionMessage() {
    }

    public InjectionMessage(String jobId, String error, String message) {
        this.jobId = jobId;
        this.error = error;
        this.message = message;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
