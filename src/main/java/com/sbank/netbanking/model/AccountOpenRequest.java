package com.sbank.netbanking.model;

public class AccountOpenRequest {
    private long requestId;
    private long userId;
    private long branchId;
    private Status status;
    private long reviewer;
    

	// Enum for status
    public enum Status {
        ACCEPTED,
        FAILED,
        PENDING
    }

    public long getRequestId() {
		return requestId;
	}

	public void setRequestId(long requestId) {
		this.requestId = requestId;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public long getBranchId() {
		return branchId;
	}

	public void setBranchId(long branchId) {
		this.branchId = branchId;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public long getReviewer() {
		return reviewer;
	}

	public void setReviewer(long reviewer) {
		this.reviewer = reviewer;
	}


}
