package com.sbank.netbanking.model;

public class Account {

    public enum AccountStatus {
        ACTIVE, INACTIVE
    }

    private Long accountNumber;
    private Long userId;
    private Double balance;
    private Long branchId;
    private AccountStatus status;
    private Long createdAt;
    private Long modifiedAt;
    private Long modifiedBy;
    
	public Long getAccountNumber() {
		return accountNumber;
	}
	public void setAccountNumber(Long accountNumber) {
		this.accountNumber = accountNumber;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public Double getBalance() {
		return balance;
	}
	public void setBalance(Double balance) {
		this.balance = balance;
	}
	public Long getBranchId() {
		return branchId;
	}
	public void setBranchId(Long branchId) {
		this.branchId = branchId;
	}
	public AccountStatus getStatus() {
		return status;
	}
	public void setStatus(AccountStatus status) {
		this.status = status;
	}
	public Long getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(Long createdAt) {
		this.createdAt = createdAt;
	}
	public Long getModifiedAt() {
		return modifiedAt;
	}
	public void setModifiedAt(Long modifiedAt) {
		this.modifiedAt = modifiedAt;
	}
	public Long getModifiedBy() {
		return modifiedBy;
	}
	public void setModifiedBy(Long modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

}
