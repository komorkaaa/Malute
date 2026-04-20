package com.example.smartlabactivity.api.dto;

import java.util.Map;

public class ErrorResponse {
    public int status;
    public String message;
    public Map<String, Object> data;
    public Map<String, String> validationErrors;
}