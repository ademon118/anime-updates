package com.aura.anime_updates.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private String token;
    private boolean success;
    private String message;
    private Integer code;
    private T data;

    public ApiResponse() {
    }

    // Success response with data
    public static <T> ApiResponse<T> success(T data, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setMessage(message);
        response.setCode(200);
        response.setData(data);
        return response;
    }

    // Success response without data
    public static <T> ApiResponse<T> success(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setMessage(message);
        response.setCode(200);
        return response;
    }

    // Success response with token
    public static <T> ApiResponse<T> successWithToken(String token, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setMessage(message);
        response.setCode(200);
        response.setToken(token);
        return response;
    }

    // Error response
    public static <T> ApiResponse<T> error(String message, Integer code) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage(message);
        response.setCode(code);
        return response;
    }

    // Error response with data
    public static <T> ApiResponse<T> error(String message, Integer code, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage(message);
        response.setCode(code);
        response.setData(data);
        return response;
    }
}
