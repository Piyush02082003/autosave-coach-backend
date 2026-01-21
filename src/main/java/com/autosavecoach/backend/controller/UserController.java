package com.autosavecoach.backend.controller;

import com.autosavecoach.backend.dto.LoginRequest;
import com.autosavecoach.backend.dto.LoginResponse;
import com.autosavecoach.backend.dto.UserResponse;
import com.autosavecoach.backend.model.User;
import com.autosavecoach.backend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // SIGN-UP
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {

        User savedUser = userService.createUser(user);
        UserResponse response =  new UserResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<LoginResponse > login(@RequestBody LoginRequest request){
        User user = userService.login(request);
        String token = userService.generateTokenForUser(user);

        LoginResponse response = new LoginResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                token
        );

        return ResponseEntity.ok(response);
    }
}
