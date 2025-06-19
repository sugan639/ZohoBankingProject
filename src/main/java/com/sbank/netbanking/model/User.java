
package com.sbank.netbanking.model;

public class User {
    private long userId;
    private String name;
    private String password;
    private String email;
    private long mobileNumber;
    private Role role;
    private long createdAt;
    private long modifiedAt;
    private long modifiedBy;

    // Enum for role
    public enum Role {
        CUSTOMER, EMPLOYEE, ADMIN
    }

    // Constructors
    public User() {}

    public User(long userId, String name, String password, String email,
                  long mobileNumber, Role role, long createdAt, long modifiedAt, long modifiedBy) {
        this.userId = userId;
        this.name = name;
        this.password = password;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.role = role;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
        this.modifiedBy = modifiedBy;
    }

    // Getters and Setters

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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