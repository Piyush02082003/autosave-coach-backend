package com.autosavecoach.backend.dto;

import lombok.Getter;

import java.util.UUID;

@Getter
public class UserResponse {

    private final UUID id;
    private final String name;
    private final String email;

    public UserResponse(UUID id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

}

