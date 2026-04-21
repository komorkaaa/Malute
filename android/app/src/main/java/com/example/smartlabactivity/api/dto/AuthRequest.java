package com.example.smartlabactivity.api.dto;

import com.google.gson.annotations.SerializedName;

public class AuthRequest {
    @SerializedName("identity")
    public String identity;

    @SerializedName("password")
    public String password;

    public AuthRequest(String identity, String password) {
        this.identity = identity;
        this.password = password;
    }
}
