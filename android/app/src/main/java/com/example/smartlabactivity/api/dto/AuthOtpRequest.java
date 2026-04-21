package com.example.smartlabactivity.api.dto;

public class AuthOtpRequest {
    public String otpId;
    public String password;

    public AuthOtpRequest(String otpId, String password) {
        this.otpId = otpId;
        this.password = password;
    }
}
