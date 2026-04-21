package com.example.smartlabactivity.api;

import com.example.smartlabactivity.api.dto.AuthRequest;
import com.example.smartlabactivity.api.dto.ProjectRecordResponse;
import com.example.smartlabactivity.api.dto.ProjectsListResponse;
import com.example.smartlabactivity.api.dto.TokenResponse;
import com.example.smartlabactivity.api.dto.UpdateUserRequest;
import com.example.smartlabactivity.api.dto.UserRecordRequest;
import com.example.smartlabactivity.api.dto.UserRecordResponse;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

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

    @GET("api/collections/projects/records")
    Call<ProjectsListResponse> getProjects(
            @Header("Authorization") String authorization,
            @Query("filter") String filter,
            @Query("sort") String sort
    );

    @GET("api/collections/projects/records/{projectId}")
    Call<ProjectRecordResponse> getProjectById(
            @Path("projectId") String projectId,
            @Header("Authorization") String authorization
    );

    @Multipart
    @POST("api/collections/projects/records")
    Call<ProjectRecordResponse> createProject(
            @Header("Authorization") String authorization,
            @Part("title") RequestBody title,
            @Part("type") RequestBody type,
            @Part("date_start") RequestBody dateStart,
            @Part("date_end") RequestBody dateEnd,
            @Part("size") RequestBody size,
            @Part("description_source") RequestBody descriptionSource,
            @Part("user_id") RequestBody userId
    );
}
