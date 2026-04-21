package com.example.smartlabactivity.api.dto;

import java.util.List;

public class BasketRequest {
    public String user_id;
    public List<BasketItemDto> items;
    public int count;

    public BasketRequest(String userId, List<BasketItemDto> items, int count) {
        this.user_id = userId;
        this.items = items;
        this.count = count;
    }
}
