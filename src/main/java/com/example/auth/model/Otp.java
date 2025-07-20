package com.example.auth.model;

public class Otp {

    private String phone;
    private String code;

    public Otp() {}

    public Otp(String phone, String code) {
        this.phone = phone;
        this.code = code;
    }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}