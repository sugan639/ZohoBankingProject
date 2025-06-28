package com.sbank.netbanking.model;

public class Transaction {

    public enum TransactionType {
        DEPOSIT,
        WITHDRAWAL,
        INTRA_BANK_DEBIT,
        INTRA_BANK_CREDIT,
        INTERBANK_DEBIT,
        INTERBANK_CREDIT
    }

    public enum TransactionStatus {
        SUCCESS,
        FAILED
    }
    private Long transactionReferenceNumber;
    private Long transactionId;
    private Long accountNumber;
    private Double amount;
    private TransactionType type;
    private TransactionStatus status;
    private Long timestamp;
    private Long doneBy;
    private Double closingBalance;
    private Long beneficiaryId;
    
    
    public Long getBeneficiaryId() {
		return beneficiaryId;
	}
	public void setBeneficiaryId(Long beneficiaryId) {
		this.beneficiaryId = beneficiaryId;
	}
	public Long getBeneficiaryAccountNumber() {
		return beneficiaryAccountNumber;
	}
	public void setBeneficiaryAccountNumber(Long beneficiaryAccountNumber) {
		this.beneficiaryAccountNumber = beneficiaryAccountNumber;
	}
	public String getIfscCode() {
		return ifscCode;
	}
	public void setIfscCode(String ifscCode) {
		this.ifscCode = ifscCode;
	}
	private Long userId;
    private Long beneficiaryAccountNumber;
    private String ifscCode;

    
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
	public double getAmount() {
		return amount;
	}
	public void setAmount(double amount) {
		this.amount = amount;
	}
	public TransactionType getType() {
		return type;
	}
	public void setType(TransactionType type) {
		this.type = type;
	}
	public TransactionStatus getStatus() {
		return status;
	}
	public void setStatus(TransactionStatus status) {
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
	public double getClosingBalance() {
		return closingBalance;
	}
	public void setClosingBalance(double closingBalance) {
		this.closingBalance = closingBalance;
	}

    
    
}
