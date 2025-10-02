package com.example.foodapp.service;

import com.example.foodapp.model.User;

public interface UserService {
    User saveUser(User user);
    User findByEmail(String email);
    boolean existsByEmail(String email);
}
