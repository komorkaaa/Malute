package com.example.smartlabactivity.api.dto;

import com.google.gson.annotations.SerializedName;

public class UserRecordRequest {
    @SerializedName("email")
    public String email;

    @SerializedName("password")
    public String password;

    @SerializedName("passwordConfirm")
    public String passwordConfirm;

    @SerializedName("emailVisibility")
    public boolean emailVisibility;

    @SerializedName("verified")
    public boolean verified;

    public UserRecordRequest(String email, String password, String passwordConfirm) {
        this.email = email;
        this.password = password;
        this.passwordConfirm = passwordConfirm;
        this.emailVisibility = true;
        this.verified = false;
    }
}
