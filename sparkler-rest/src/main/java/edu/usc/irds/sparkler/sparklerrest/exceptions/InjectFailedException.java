package edu.usc.irds.sparkler.sparklerrest.exceptions;

public class InjectFailedException extends Exception{
    String msg;
    String jobId;
    public InjectFailedException(String errorMessage, String message, String jobId){
        super(errorMessage);
        this.msg = message;
        this.jobId = jobId;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }
}
