package com.example.foodapp.controller;

import com.example.foodapp.dto.LoginRequest;
import com.example.foodapp.dto.LoginResponse;
import com.example.foodapp.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    // For this first step we'll do a simple hardcoded check.
    // Later we'll replace with a proper UserDetailsService + password hashing.
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        if ("admin".equals(req.getUsername()) && "password".equals(req.getPassword())) {
            String token = jwtUtil.generateToken(req.getUsername());
            return ResponseEntity.ok(new LoginResponse(token));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\":\"Invalid credentials\"}");
    }
}
