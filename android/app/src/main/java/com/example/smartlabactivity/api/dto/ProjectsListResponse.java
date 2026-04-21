package com.example.smartlabactivity.api.dto;

import java.util.List;

public class ProjectsListResponse {
    public int page;
    public int perPage;
    public int totalPages;
    public int totalItems;
    public List<ProjectRecordResponse> items;
}
