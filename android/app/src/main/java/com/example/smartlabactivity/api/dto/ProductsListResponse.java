package com.example.smartlabactivity.api.dto;

import java.util.List;

public class ProductsListResponse {
    public int page;
    public int perPage;
    public int totalPages;
    public int totalItems;
    public List<ProductRecordResponse> items;
}
