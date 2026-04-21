package com.example.smartlabactivity.api;

import com.example.smartlabactivity.api.dto.BasketItemDto;
import com.example.smartlabactivity.api.dto.ErrorResponse;
import com.example.smartlabactivity.api.dto.OrderRecordResponse;
import com.example.smartlabactivity.api.dto.OrderRequest;
import com.example.smartlabactivity.api.dto.OrdersListResponse;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderRepository {
    private final ApiService apiService;
    private final Gson gson;

    public OrderRepository() {
        apiService = ApiClient.getApiService();
        gson = new Gson();
    }

    public void getOrders(String token, final OrdersCallback callback) {
        apiService.getOrders("Bearer " + token, 1, 30).enqueue(new Callback<OrdersListResponse>() {
            @Override
            public void onResponse(Call<OrdersListResponse> call, Response<OrdersListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(parseErrorResponse(response));
                }
            }

            @Override
            public void onFailure(Call<OrdersListResponse> call, Throwable t) {
                callback.onError("Сетевая ошибка: " + t.getMessage());
            }
        });
    }

    public void createOrder(
            String token,
            String userId,
            List<BasketItemDto> items,
            double total,
            final OrderCallback callback
    ) {
        apiService.createOrder("Bearer " + token, new OrderRequest(userId, items, total))
                .enqueue(new OrderResponseCallback(callback));
    }

    public void getOrderById(String token, String orderId, final OrderCallback callback) {
        apiService.getOrderById(orderId, "Bearer " + token)
                .enqueue(new OrderResponseCallback(callback));
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

    private final class OrderResponseCallback implements Callback<OrderRecordResponse> {
        private final OrderCallback callback;

        private OrderResponseCallback(OrderCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onResponse(Call<OrderRecordResponse> call, Response<OrderRecordResponse> response) {
            if (response.isSuccessful() && response.body() != null) {
                callback.onSuccess(response.body());
            } else {
                callback.onError(parseErrorResponse(response));
            }
        }

        @Override
        public void onFailure(Call<OrderRecordResponse> call, Throwable t) {
            callback.onError("Сетевая ошибка: " + t.getMessage());
        }
    }

    public interface OrdersCallback {
        void onSuccess(OrdersListResponse response);
        void onError(String error);
    }

    public interface OrderCallback {
        void onSuccess(OrderRecordResponse order);
        void onError(String error);
    }
}
