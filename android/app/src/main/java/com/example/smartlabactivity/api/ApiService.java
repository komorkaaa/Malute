package com.example.smartlabactivity.api;

import com.example.smartlabactivity.api.dto.AuthRequest;
import com.example.smartlabactivity.api.dto.TokenResponse;
import com.example.smartlabactivity.api.dto.UpdateUserRequest;
import com.example.smartlabactivity.api.dto.UserRecordRequest;
import com.example.smartlabactivity.api.dto.UserRecordResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Header;

public interface ApiService {

    @POST("api/collections/users/records")
    Call<UserRecordResponse> registerUser(@Body UserRecordRequest request);

    @POST("api/collections/users/auth-with-password")
    Call<TokenResponse> authUser(@Body AuthRequest request);

    @PATCH("api/collections/users/records/{userId}")
    Call<UserRecordResponse> updateUser(
            @Path("userId") String userId,
            @Header("Authorization") String authorization,
            @Body UpdateUserRequest request
    );
}
