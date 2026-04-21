package com.example.smartlabactivity.api;

import com.example.smartlabactivity.api.dto.ErrorResponse;
import com.example.smartlabactivity.api.dto.NewsListResponse;
import com.example.smartlabactivity.api.dto.ProductRecordResponse;
import com.example.smartlabactivity.api.dto.ProductsListResponse;
import com.google.gson.Gson;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShopRepository {
    private final ApiService apiService;
    private final Gson gson;

    public ShopRepository() {
        apiService = ApiClient.getApiService();
        gson = new Gson();
    }

    public void getProducts(String token, String filter, final ProductsCallback callback) {
        apiService.getProducts("Bearer " + token, filter, "-created")
                .enqueue(new Callback<ProductsListResponse>() {
                    @Override
                    public void onResponse(Call<ProductsListResponse> call, Response<ProductsListResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body());
                        } else {
                            callback.onError(parseErrorResponse(response));
                        }
                    }

                    @Override
                    public void onFailure(Call<ProductsListResponse> call, Throwable t) {
                        callback.onError("Сетевая ошибка: " + t.getMessage());
                    }
                });
    }

    public void getNews(String token, final NewsCallback callback) {
        apiService.getNewsAndPromotions("Bearer " + token)
                .enqueue(new Callback<NewsListResponse>() {
                    @Override
                    public void onResponse(Call<NewsListResponse> call, Response<NewsListResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body());
                        } else {
                            callback.onError(parseErrorResponse(response));
                        }
                    }

                    @Override
                    public void onFailure(Call<NewsListResponse> call, Throwable t) {
                        callback.onError("Сетевая ошибка: " + t.getMessage());
                    }
                });
    }

    public void getProductById(String token, String productId, final ProductCallback callback) {
        apiService.getProductById(productId, "Bearer " + token)
                .enqueue(new Callback<ProductRecordResponse>() {
                    @Override
                    public void onResponse(Call<ProductRecordResponse> call, Response<ProductRecordResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body());
                        } else {
                            callback.onError(parseErrorResponse(response));
                        }
                    }

                    @Override
                    public void onFailure(Call<ProductRecordResponse> call, Throwable t) {
                        callback.onError("Сетевая ошибка: " + t.getMessage());
                    }
                });
    }

    private String parseErrorResponse(Response<?> response) {
        String errorMessage = "Ошибка: " + response.code();

        if (response.errorBody() != null) {
            try {
                String errorBody = response.errorBody().string();
                ErrorResponse errorResponse = gson.fromJson(errorBody, ErrorResponse.class);
                if (errorResponse != null && errorResponse.message != null) {
                    errorMessage = errorResponse.message;
                }
            } catch (IOException ignored) {
            }
        }

        return errorMessage;
    }

    public interface ProductsCallback {
        void onSuccess(ProductsListResponse response);
        void onError(String error);
    }

    public interface NewsCallback {
        void onSuccess(NewsListResponse response);
        void onError(String error);
    }

    public interface ProductCallback {
        void onSuccess(ProductRecordResponse product);
        void onError(String error);
    }
}
