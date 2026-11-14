package com.hotel.exception;

/**
 * Thrown when the data file cannot be read or contains an invalid line.
 * The original exception (IOException, NumberFormatException...) is kept as the cause.
 */
public class DataLoadingException extends Exception {

    public DataLoadingException(String message) {
        super(message);
    }

    public DataLoadingException(String message, Throwable cause) {
        super(message, cause);
    }
}
