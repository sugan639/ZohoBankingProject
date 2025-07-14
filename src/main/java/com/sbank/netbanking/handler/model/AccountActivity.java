package com.sbank.netbanking.handler.model;


public class AccountActivity {
    private long accountNumber;
    private long txnCount;

    public AccountActivity() {}

    public AccountActivity(long accountNumber, long txnCount) {
        this.accountNumber = accountNumber;
        this.txnCount = txnCount;
    }

    public long getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(long accountNumber) {
        this.accountNumber = accountNumber;
    }

    public long getTxnCount() {
        return txnCount;
    }

    public void setTxnCount(long txnCount) {
        this.txnCount = txnCount;
    }
}
