package com.aura.anime_updates.dto;

import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private boolean success;
    private String message;

    public AuthResponse(){

    }

    public AuthResponse(String token){
        this.token = token;
        this.success = true;
        this.message = "Authentication successful";
    }

    public AuthResponse(boolean success, String message){
        this.success = success;
        this.message = message;
    }

    public AuthResponse(boolean success, String message, String token){
        this.success = success;
        this.message = message;
        this.token = token;
    }

    // Explicit getters to ensure compatibility even without Lombok processing
    public boolean isSuccess() {
        return success;
    }

    public boolean getSuccess() {
        return success;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }



}
