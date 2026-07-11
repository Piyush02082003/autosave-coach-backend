package com.autosavecoach.backend.dto.response;

import lombok.Getter;

import java.util.UUID;

@Getter
public class LoginResponse {

    private final UUID id;
    private final String name;
    private final String email;
    private final String token;

    public LoginResponse(UUID id, String name, String email, String token){
        this.id = id;
        this.name = name;
        this.email = email;
        this.token = token;
    }

}
