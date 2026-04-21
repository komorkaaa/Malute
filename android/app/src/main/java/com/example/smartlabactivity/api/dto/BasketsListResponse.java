package com.example.smartlabactivity.api.dto;

import java.util.List;

public class BasketsListResponse {
    public int page;
    public int perPage;
    public int totalPages;
    public int totalItems;
    public List<BasketRecordResponse> items;
}
