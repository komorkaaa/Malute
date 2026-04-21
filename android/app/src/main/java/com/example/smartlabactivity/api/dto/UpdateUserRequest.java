package com.example.smartlabactivity.api.dto;

import com.google.gson.annotations.SerializedName;

public class UpdateUserRequest {
    @SerializedName("first_name")
    public String firstName;

    @SerializedName("last_name")
    public String lastName;

    @SerializedName("patronymic")
    public String patronymic;

    @SerializedName("birthday")
    public String birthday;

    @SerializedName("gender")
    public String gender;

    public UpdateUserRequest(
            String firstName,
            String lastName,
            String patronymic,
            String birthday,
            String gender
    ) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.patronymic = patronymic;
        this.birthday = birthday;
        this.gender = gender;
    }
}
