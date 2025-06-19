package com.sbank.netbanking.exceptions;


public class TaskException extends Exception{
    
    // Serial version UID 
    private static final long serialVersionUID = 1L;

    public TaskException(String message) {
        super(message);
    }

    public TaskException(String message, Throwable cause) {
        super(message, cause);
    }

    public TaskException(Throwable cause) {
        super(cause);
    }
}