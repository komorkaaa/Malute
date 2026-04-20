package com.example.smartlabactivity.api;

import com.example.smartlabactivity.api.dto.TokenResponse;
import com.example.smartlabactivity.api.dto.UserRecordRequest;
import com.example.smartlabactivity.api.dto.UserRecordResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("api/collections/users/records")
    Call<UserRecordResponse> registerUser(@Body UserRecordRequest request);
}