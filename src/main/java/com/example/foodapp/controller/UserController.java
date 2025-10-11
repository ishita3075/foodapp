package com.example.foodapp.controller;

import com.example.foodapp.model.User;
import com.example.foodapp.security.JwtUtil;
import com.example.foodapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users") // Changed from /auth to /users
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // User Registration (optional, can remove if handled by AuthController)
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        if (userService.existsByEmail(user.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\":\"Email already registered\"}");
        }

        // Hash password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userService.saveUser(user);

        return ResponseEntity.ok("{\"message\":\"User registered successfully\"}");
    }

    // User Login (optional, can remove if handled by AuthController)
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User user) {
        User existingUser = userService.findByEmail(user.getEmail());
        if (existingUser != null && passwordEncoder.matches(user.getPassword(), existingUser.getPassword())) {
            String token = jwtUtil.generateToken(existingUser.getEmail());
            return ResponseEntity.ok("{\"token\":\"" + token + "\"}");
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("{\"error\":\"Invalid email or password\"}");
    }
}
