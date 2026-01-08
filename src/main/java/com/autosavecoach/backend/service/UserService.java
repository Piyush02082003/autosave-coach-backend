package com.autosavecoach.backend.service;

import com.autosavecoach.backend.model.User;
import com.autosavecoach.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService{
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public User createUser(User user){
        return userRepository.save(user);
    }
}