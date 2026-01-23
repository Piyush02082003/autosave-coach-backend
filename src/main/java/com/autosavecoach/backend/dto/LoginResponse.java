package com.autosavecoach.backend.dto;

import lombok.Getter;

@Getter
public class LoginResponse {

    private final Long id;
    private final String name;
    private final String email;
    private final String token;

    public LoginResponse(Long id, String name, String email, String token){
        this.id = id;
        this.name = name;
        this.email = email;
        this.token = token;
    }

}
