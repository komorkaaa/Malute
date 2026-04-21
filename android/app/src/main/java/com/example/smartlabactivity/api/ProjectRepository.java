package com.example.smartlabactivity.api;

import com.example.smartlabactivity.api.dto.ErrorResponse;
import com.example.smartlabactivity.api.dto.ProjectRecordResponse;
import com.example.smartlabactivity.api.dto.ProjectsListResponse;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProjectRepository {
    private final ApiService apiService;
    private final Gson gson;

    public ProjectRepository() {
        apiService = ApiClient.getApiService();
        gson = new Gson();
    }

    public void getProjects(String token, String userId, final ProjectsCallback callback) {
        String filter = null;
        if (userId != null && !userId.isEmpty()) {
            filter = "user_id=\"" + userId + "\"";
        }

        apiService.getProjects("Bearer " + token, filter, "-created")
                .enqueue(new Callback<ProjectsListResponse>() {
                    @Override
                    public void onResponse(Call<ProjectsListResponse> call, Response<ProjectsListResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body());
                        } else {
                            callback.onError(parseErrorResponse(response));
                        }
                    }

                    @Override
                    public void onFailure(Call<ProjectsListResponse> call, Throwable t) {
                        callback.onError("Сетевая ошибка: " + t.getMessage());
                    }
                });
    }

    public void getProjectById(String token, String projectId, final ProjectCallback callback) {
        apiService.getProjectById(projectId, "Bearer " + token)
                .enqueue(new Callback<ProjectRecordResponse>() {
                    @Override
                    public void onResponse(Call<ProjectRecordResponse> call, Response<ProjectRecordResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body());
                        } else {
                            callback.onError(parseErrorResponse(response));
                        }
                    }

                    @Override
                    public void onFailure(Call<ProjectRecordResponse> call, Throwable t) {
                        callback.onError("Сетевая ошибка: " + t.getMessage());
                    }
                });
    }

    public void createProject(
            String token,
            String userId,
            String title,
            String type,
            String dateStart,
            String dateEnd,
            String assignee,
            String source,
            String category,
            final ProjectCallback callback
    ) {
        apiService.createProject(
                "Bearer " + token,
                part(title),
                part(type),
                part(dateStart),
                part(dateEnd),
                part(category),
                part("Кому: " + assignee + "\nИсточник: " + source),
                part(userId)
        ).enqueue(new Callback<ProjectRecordResponse>() {
            @Override
            public void onResponse(Call<ProjectRecordResponse> call, Response<ProjectRecordResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(parseErrorResponse(response));
                }
            }

            @Override
            public void onFailure(Call<ProjectRecordResponse> call, Throwable t) {
                callback.onError("Сетевая ошибка: " + t.getMessage());
            }
        });
    }

    private RequestBody part(String value) {
        return RequestBody.create(value == null ? "" : value, MediaType.parse("text/plain"));
    }

    private String parseErrorResponse(Response<?> response) {
        String errorMessage = "Ошибка: " + response.code();

        if (response.errorBody() != null) {
            try {
                String errorBody = response.errorBody().string();
                ErrorResponse errorResponse = gson.fromJson(errorBody, ErrorResponse.class);
                if (errorResponse != null && errorResponse.message != null) {
                    errorMessage = errorResponse.message;
                }
            } catch (IOException ignored) {
            }
        }

        return errorMessage;
    }

    public interface ProjectsCallback {
        void onSuccess(ProjectsListResponse response);
        void onError(String error);
    }

    public interface ProjectCallback {
        void onSuccess(ProjectRecordResponse project);
        void onError(String error);
    }
}
