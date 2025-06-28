package com.sbank.netbanking.model;

public class Account {

    public enum AccountStatus {
        ACTIVE, INACTIVE
    }

    private long accountNumber;
    private long userId;
    private double balance;
    private long branchId;
    private AccountStatus status;
    private long createdAt;
    private long modifiedAt;
    private long modifiedBy;

    public long getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(long accountNumber) {
        this.accountNumber = accountNumber;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public long getBranchId() {
        return branchId;
    }

    public void setBranchId(long branchId) {
        this.branchId = branchId;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus transactionStatus) {
        this.status = transactionStatus;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(long modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public long getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(long modifiedBy) {
        this.modifiedBy = modifiedBy;
    }
}
