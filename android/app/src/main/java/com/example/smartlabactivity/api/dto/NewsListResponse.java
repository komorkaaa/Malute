package com.example.smartlabactivity.api.dto;

import java.util.List;

public class NewsListResponse {
    public int page;
    public int perPage;
    public int totalPages;
    public int totalItems;
    public List<NewsRecordResponse> items;
}
