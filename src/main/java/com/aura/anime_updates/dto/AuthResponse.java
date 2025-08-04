package com.aura.anime_updates.dto;

import lombok.Data;

public class AuthResponse {
    private String token;

    public AuthResponse(){

    }

    public AuthResponse(String token){
        this.token = token;
    }

    public String getToken(){
        return this.token;
    }

    public void setToken(String token){
        this.token = token;
    }

}
