package com.example.smartlabactivity.api;

import com.example.smartlabactivity.api.dto.BasketItemDto;
import com.example.smartlabactivity.api.dto.BasketRecordResponse;
import com.example.smartlabactivity.api.dto.BasketRequest;
import com.example.smartlabactivity.api.dto.BasketUpdateRequest;
import com.example.smartlabactivity.api.dto.BasketsListResponse;
import com.example.smartlabactivity.api.dto.ErrorResponse;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BasketRepository {
    private final ApiService apiService;
    private final Gson gson;

    public BasketRepository() {
        apiService = ApiClient.getApiService();
        gson = new Gson();
    }

    public void getCurrentBasket(String token, final BasketCallback callback) {
        apiService.getBasket("Bearer " + token).enqueue(new Callback<BasketsListResponse>() {
            @Override
            public void onResponse(Call<BasketsListResponse> call, Response<BasketsListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BasketsListResponse body = response.body();
                    BasketRecordResponse basket = null;
                    if (body.items != null && !body.items.isEmpty()) {
                        basket = body.items.get(0);
                    }
                    callback.onSuccess(basket);
                } else {
                    callback.onError(parseErrorResponse(response));
                }
            }

            @Override
            public void onFailure(Call<BasketsListResponse> call, Throwable t) {
                callback.onError("Сетевая ошибка: " + t.getMessage());
            }
        });
    }

    public void createBasket(
            String token,
            String userId,
            List<BasketItemDto> items,
            int count,
            final BasketCallback callback
    ) {
        apiService.createBasket("Bearer " + token, new BasketRequest(userId, items, count))
                .enqueue(new BasketResponseCallback(callback));
    }

    public void getBasketById(String token, String basketId, final BasketCallback callback) {
        apiService.getBasketById(basketId, "Bearer " + token)
                .enqueue(new BasketResponseCallback(callback));
    }

    public void updateBasket(
            String token,
            String basketId,
            String userId,
            List<BasketItemDto> items,
            int count,
            final BasketCallback callback
    ) {
        apiService.updateBasket(
                basketId,
                "Bearer " + token,
                new BasketUpdateRequest(userId, items, count)
        ).enqueue(new BasketResponseCallback(callback));
    }

    public void deleteBasket(String token, String basketId, final SimpleCallback callback) {
        apiService.deleteBasket(basketId, "Bearer " + token).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onError(parseErrorResponse(response));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Сетевая ошибка: " + t.getMessage());
            }
        });
    }

    private String parseErrorResponse(Response<?> response) {
        String errorMessage = "Ошибка: " + response.code();
        if (response.errorBody() != null) {
            try {
                ErrorResponse errorResponse = gson.fromJson(response.errorBody().string(), ErrorResponse.class);
                if (errorResponse != null && errorResponse.message != null) {
                    errorMessage = errorResponse.message;
                }
            } catch (IOException ignored) {
            }
        }
        return errorMessage;
    }

    private final class BasketResponseCallback implements Callback<BasketRecordResponse> {
        private final BasketCallback callback;

        private BasketResponseCallback(BasketCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onResponse(Call<BasketRecordResponse> call, Response<BasketRecordResponse> response) {
            if (response.isSuccessful() && response.body() != null) {
                callback.onSuccess(response.body());
            } else {
                callback.onError(parseErrorResponse(response));
            }
        }

        @Override
        public void onFailure(Call<BasketRecordResponse> call, Throwable t) {
            callback.onError("Сетевая ошибка: " + t.getMessage());
        }
    }

    public interface BasketCallback {
        void onSuccess(BasketRecordResponse basket);
        void onError(String error);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onError(String error);
    }
}
