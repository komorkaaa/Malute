package com.example.smartlabactivity.api;

import com.example.smartlabactivity.api.dto.AuthRequest;
import com.example.smartlabactivity.api.dto.AuthOtpRequest;
import com.example.smartlabactivity.api.dto.BasketRecordResponse;
import com.example.smartlabactivity.api.dto.BasketRequest;
import com.example.smartlabactivity.api.dto.BasketUpdateRequest;
import com.example.smartlabactivity.api.dto.BasketsListResponse;
import com.example.smartlabactivity.api.dto.NewsListResponse;
import com.example.smartlabactivity.api.dto.OrderRecordResponse;
import com.example.smartlabactivity.api.dto.OrderRequest;
import com.example.smartlabactivity.api.dto.OrdersListResponse;
import com.example.smartlabactivity.api.dto.ProductRecordResponse;
import com.example.smartlabactivity.api.dto.ProjectRecordResponse;
import com.example.smartlabactivity.api.dto.ProjectsListResponse;
import com.example.smartlabactivity.api.dto.ProductsListResponse;
import com.example.smartlabactivity.api.dto.RequestOtpRequest;
import com.example.smartlabactivity.api.dto.ResponseOtp;
import com.example.smartlabactivity.api.dto.TokenResponse;
import com.example.smartlabactivity.api.dto.UpdateUserRequest;
import com.example.smartlabactivity.api.dto.UserRecordRequest;
import com.example.smartlabactivity.api.dto.UserRecordResponse;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
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

    @POST("api/collections/users/request-otp")
    Call<ResponseOtp> requestOtp(@Body RequestOtpRequest request);

    @POST("api/collections/users/auth-with-otp")
    Call<TokenResponse> authWithOtp(@Body AuthOtpRequest request);

    @GET("api/collections/users/records/{userId}")
    Call<UserRecordResponse> getUserById(
            @Path("userId") String userId,
            @Header("Authorization") String authorization
    );

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

    @GET("api/collections/products/records")
    Call<ProductsListResponse> getProducts(
            @Header("Authorization") String authorization,
            @Query("filter") String filter,
            @Query("sort") String sort
    );

    @GET("api/collections/products/records/{productId}")
    Call<ProductRecordResponse> getProductById(
            @Path("productId") String productId,
            @Header("Authorization") String authorization
    );

    @GET("api/collections/basket/records")
    Call<BasketsListResponse> getBasket(
            @Header("Authorization") String authorization
    );

    @POST("api/collections/basket/records")
    Call<BasketRecordResponse> createBasket(
            @Header("Authorization") String authorization,
            @Body BasketRequest request
    );

    @GET("api/collections/basket/records/{basketId}")
    Call<BasketRecordResponse> getBasketById(
            @Path("basketId") String basketId,
            @Header("Authorization") String authorization
    );

    @PATCH("api/collections/basket/records/{basketId}")
    Call<BasketRecordResponse> updateBasket(
            @Path("basketId") String basketId,
            @Header("Authorization") String authorization,
            @Body BasketUpdateRequest request
    );

    @DELETE("api/collections/basket/records/{basketId}")
    Call<Void> deleteBasket(
            @Path("basketId") String basketId,
            @Header("Authorization") String authorization
    );

    @GET("api/collections/orders/records")
    Call<OrdersListResponse> getOrders(
            @Header("Authorization") String authorization,
            @Query("page") Integer page,
            @Query("perPage") Integer perPage
    );

    @POST("api/collections/orders/records")
    Call<OrderRecordResponse> createOrder(
            @Header("Authorization") String authorization,
            @Body OrderRequest request
    );

    @GET("api/collections/orders/records/{orderId}")
    Call<OrderRecordResponse> getOrderById(
            @Path("orderId") String orderId,
            @Header("Authorization") String authorization
    );

    @GET("api/collections/promotions_and_news/records")
    Call<NewsListResponse> getNewsAndPromotions(
            @Header("Authorization") String authorization
    );
}
