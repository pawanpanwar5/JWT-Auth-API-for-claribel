package com.example.auth.dto;

public class RegisterRequest {
    private String name;
    private String emailId;
    private String username;
    private String password;
    private String phoneNumber;
    //private Long defaultRoleId;

    // --- Getters and Setters ---
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

//    public Long getDefaultRoleId() {
//        return defaultRoleId;
//    }
//
//    public void setDefaultRoleId(Long defaultRoleId) {
//        this.defaultRoleId = defaultRoleId;
//    }
}
