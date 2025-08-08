package com.sbank.netbanking.model;

public class SessionData {
    private long userId;
    private String sessionID;
    private long startTime;
    private long expiryDuration;
    private long id;
	 


	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getSessionID() {
		return sessionID;
	}

	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}


	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getExpiryDuration() {
		return expiryDuration;
	}

	public void setExpiryDuration(long expiryDuration) {
		this.expiryDuration = expiryDuration;
	}
	
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}


	

	@Override
	public String toString() {
		
		String session =  "Session ID: " + sessionID +"\n " +
						  "UserId: " + userId + " \n"+
						  "Start Time: " + startTime + "\n "+
						  "Expiry duration: " +  expiryDuration;
				
				
			
				
		return session;
	}





    
    
}
