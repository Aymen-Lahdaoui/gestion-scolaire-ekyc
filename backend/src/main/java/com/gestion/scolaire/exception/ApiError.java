package com.gestion.scolaire.exception;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

public record ApiError(
        int status,
        String error,
        String message,
        String path,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime timestamp,
        List<String> validationErrors
) {
    public ApiError(int status, String error, String message, String path) {
        this(status, error, message, path, LocalDateTime.now(), null);
    }

    public ApiError(int status, String error, String message, String path, List<String> validationErrors) {
        this(status, error, message, path, LocalDateTime.now(), validationErrors);
    }
}
