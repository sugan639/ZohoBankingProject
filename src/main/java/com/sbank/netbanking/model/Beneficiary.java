package com.sbank.netbanking.model;

public class Beneficiary {
	

    private long beneficiaryUniqueNumber;
    private long customerId;
    private long beneficiaryAccountNumber;
    private String beneficiaryName;
    private String beneficiaryIfscCode;
    private long createdAt;
    private long modifiedAt;
    private long modifiedBy;
    
	public long getBeneficiaryUniqueNumber() {
		return beneficiaryUniqueNumber;
	}
	public void setBeneficiaryUniqueNumber(long beneficiaryUniqueNumber) {
		this.beneficiaryUniqueNumber = beneficiaryUniqueNumber;
	}
	public long getCustomerId() {
		return customerId;
	}
	public void setCustomerId(long customerId) {
		this.customerId = customerId;
	}
	public long getBeneficiaryAccountNumber() {
		return beneficiaryAccountNumber;
	}
	public void setBeneficiaryAccountNumber(long beneficiaryAccountNumber) {
		this.beneficiaryAccountNumber = beneficiaryAccountNumber;
	}
	public String getBeneficiaryName() {
		return beneficiaryName;
	}
	public void setBeneficiaryName(String beneficiaryName) {
		this.beneficiaryName = beneficiaryName;
	}
	public String getBeneficiaryIfscCode() {
		return beneficiaryIfscCode;
	}
	public void setBeneficiaryIfscCode(String beneficiaryIfscCode) {
		this.beneficiaryIfscCode = beneficiaryIfscCode;
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
