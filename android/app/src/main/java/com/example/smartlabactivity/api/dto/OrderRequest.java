package com.example.smartlabactivity.api.dto;

import java.util.List;

public class OrderRequest {
    public String user_id;
    public List<BasketItemDto> items;
    public double total;

    public OrderRequest(String userId, List<BasketItemDto> items, double total) {
        this.user_id = userId;
        this.items = items;
        this.total = total;
    }
}
