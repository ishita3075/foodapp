package com.example.foodapp.controller;

import com.example.foodapp.model.User;
import com.example.foodapp.dto.LoginRequest;
import com.example.foodapp.dto.LoginResponse;
import com.example.foodapp.security.JwtUtil;
import com.example.foodapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        // Find user by email
        User user = userService.findByEmail(req.getEmail());

        if (user != null && user.getPassword().equals(req.getPassword())) {
            // Generate JWT token
            String token = jwtUtil.generateToken(user.getEmail());
            return ResponseEntity.ok(new LoginResponse(token));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("{\"error\":\"Invalid email or password\"}");
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userService.existsByEmail(user.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\":\"Email already registered\"}");
        }
        userService.saveUser(user);
        return ResponseEntity.ok("{\"message\":\"User registered successfully\"}");
    }
}
