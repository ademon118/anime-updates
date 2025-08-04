package com.aura.anime_updates.dto;


import lombok.Data;


public class AuthRequest {
    private String userName;
    private String password;

    public AuthRequest(){

    }

    public AuthRequest(String userName,String password){
        this.userName = userName;
        this.password = password;
    }

    public String getUserName(){
        return this.userName;
    }

    public void setUserName(String userName){
        this.userName = userName;
    }

    public String getPassword(){
        return this.password;
    }

    public void setPassword(String password){
        this.password = password;
    }
}
