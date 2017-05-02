package edu.usc.irds.sparkler;

/**
 * Created by thammegr on 5/1/17.
 */
public class SparklerRuntimeException extends RuntimeException {
    public SparklerRuntimeException(String message) {
        super(message);
    }

    public SparklerRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
