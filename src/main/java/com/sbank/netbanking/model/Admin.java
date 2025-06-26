package com.sbank.netbanking.model;

import com.sbank.netbanking.model.User.Role;

public class Admin {

	    private long employeeId;         // same as user_id
	    private String name;
	    private String email;
	    private long mobileNumber;
		private Role role;
	    private long branchId;
	    private long createdAt;
	    private long modifiedAt;
	    private long modifiedBy;
	    
		public long getEmployeeId() {
			return employeeId;
		}
		public void setEmployeeId(long employeeId) {
			this.employeeId = employeeId;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getEmail() {
			return email;
		}
		public void setEmail(String email) {
			this.email = email;
		}
		public long getMobileNumber() {
			return mobileNumber;
		}
		public void setMobileNumber(long mobileNumber) {
			this.mobileNumber = mobileNumber;
		}
		public Role getRole() {
			return role;
		}
		public void setRole(Role role) {
			this.role = role;
		}
		public long getBranchId() {
			return branchId;
		}
		public void setBranchId(long branchId) {
			this.branchId = branchId;
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
