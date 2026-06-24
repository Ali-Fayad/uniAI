package com.uniai.user.application.dto.response;

public record SignUpResultDto(boolean verificationRequired, String message) {
    public static SignUpResultDto verificationRequired(String message) {
        return new SignUpResultDto(true, message);
    }
}
