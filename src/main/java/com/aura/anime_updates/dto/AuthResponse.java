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


}
