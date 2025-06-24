package com.sbank.netbanking.dto;


public class ErrorResponse {
    private String error;
    private int code;
    private String message;
	private long timestamp = System.currentTimeMillis();

    public ErrorResponse(String error, int code, String message) {
        this.error   = error;
        this.code    = code;
        this.message = message;
    }
    
    public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}


    public long getTimestamp() {
        return timestamp;
    }
   
    
}
