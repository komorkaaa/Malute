package com.example.smartlabactivity.api.dto;

import java.util.List;

public class BasketRecordResponse {
    public String id;
    public String collectionId;
    public String collectionName;
    public String created;
    public String updated;
    public String user_id;
    public List<BasketItemDto> items;
    public int count;
}
