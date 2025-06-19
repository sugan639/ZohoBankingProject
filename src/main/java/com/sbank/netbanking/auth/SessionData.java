package com.sbank.netbanking.auth;

public class SessionData {
    private int userId;
    private String sessionID;
    private Role role;
    private long startTime;
	 
    // Enum can be inside or outside the class
    public enum Role {
        ADMIN, EMPLOYEE, CUSTOMER
    }
	

	public int getUserId() {
		return userId;
	}
	
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getUsername() {
		return sessionID;
	}
	public void setUsername(String username) {
		this.sessionID = username;
	}
	
	public Role getRole() {
		return role;
	}
	public void setRole(Role role) {
		this.role = role;
	}
	
    public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}





    
    
}
