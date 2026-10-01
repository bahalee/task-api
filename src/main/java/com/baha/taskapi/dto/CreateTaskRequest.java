package com.baha.taskapi.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateTaskRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    public CreateTaskRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}