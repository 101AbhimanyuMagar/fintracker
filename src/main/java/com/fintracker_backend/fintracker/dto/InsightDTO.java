package com.fintracker_backend.fintracker.dto;

public class InsightDTO {
    private String message;

    public InsightDTO(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}