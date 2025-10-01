package com.claropr.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ApiResponseDTO<T> {
    private int status;
    private String message;
    private T data;

    public ApiResponseDTO() {}

    public ApiResponseDTO(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponseDTO<T> create(int status, String message, T data) {
        return new ApiResponseDTO<>(status, message, data);
    }
}

