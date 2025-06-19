package com.sbank.netbanking.model;

public class Transaction {

	private long transactionId;
    private long userId;
    private long accountNumber;
    private long transactionReferenceNumber;
    private float amount;
    private Type type;
    private Status status;
    private long timestamp;
    private long doneBy;
    private float closingBalance;
    
	public enum Type {
        DEPOSIT,
        WITHDRAWAL,
        INTRA_BANK_DEBIT,
        INTRA_BANK_CREDIT,
        INTERBANK_DEBIT,
        INTERBANK_CREDIT
    }

    public enum Status {
        SUCCESS,
        FAILED
    }
    
    
    
    public long getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(long transactionId) {
		this.transactionId = transactionId;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public long getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(long accountNumber) {
		this.accountNumber = accountNumber;
	}

	public long getTransactionReferenceNumber() {
		return transactionReferenceNumber;
	}

	public void setTransactionReferenceNumber(long transactionReferenceNumber) {
		this.transactionReferenceNumber = transactionReferenceNumber;
	}

	public float getAmount() {
		return amount;
	}

	public void setAmount(float amount) {
		this.amount = amount;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public long getDoneBy() {
		return doneBy;
	}

	public void setDoneBy(long doneBy) {
		this.doneBy = doneBy;
	}

	public float getClosingBalance() {
		return closingBalance;
	}

	public void setClosingBalance(float closingBalance) {
		this.closingBalance = closingBalance;
	}


    
    
    
}
