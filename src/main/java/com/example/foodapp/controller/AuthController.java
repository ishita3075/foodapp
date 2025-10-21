package com.example.foodapp.controller;
import com.example.foodapp.model.User;
import com.example.foodapp.dto.LoginRequest;
import com.example.foodapp.dto.LoginResponse;
import com.example.foodapp.security.JwtUtil;
import com.example.foodapp.service.UserService;
import com.example.foodapp.service.PasswordResetService; // ✅ added import
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordResetService passwordResetService; // ✅ added

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Signup / Register
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userService.existsByEmail(user.getEmail())) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Email already registered");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        // Hash the password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userService.saveUser(user);

        Map<String, String> success = new HashMap<>();
        success.put("message", "User registered successfully");
        return ResponseEntity.ok(success);
    }

    // Login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        User user = userService.findByEmail(req.getEmail());
        if (user != null && passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            String token = jwtUtil.generateToken(user.getEmail());

            // Create a JSON response with token and user info
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("name", user.getName());
            userInfo.put("email", user.getEmail());
            response.put("user", userInfo);

            return ResponseEntity.ok(response);
        }

        Map<String, String> error = new HashMap<>();
        error.put("error", "Invalid email or password");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    // ✅ ---------------- FORGOT + RESET PASSWORD FEATURE STARTS HERE ----------------

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        try {
            String token = passwordResetService.generateResetToken(email);
            String resetLink = "https://yourfrontend.com/reset-password?token=" + token; // change this to your frontend URL

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Password reset link generated successfully");
            response.put("resetLink", resetLink); // For testing (remove later)
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");

        try {
            passwordResetService.resetPassword(token, newPassword);
            return ResponseEntity.ok(Map.of("message", "Password reset successful"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    // ✅ ---------------- GOOGLE EMAIL VERIFICATION ENDPOINT ----------------
    @PostMapping("/verify-google-email")
    public ResponseEntity<?> verifyGoogleEmail(@RequestBody Map<String, String> request) {
        String idToken = request.get("idToken"); // token sent from frontend

        try {
            // Verify the token with Google's servers
            com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier verifier =
                    new com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier.Builder(
                            new com.google.api.client.http.javanet.NetHttpTransport(),
                            new com.google.api.client.json.gson.GsonFactory())
                            .setAudience(java.util.Collections.singletonList("241062799395-kms9mcpd3kh7410njnpubhoott8o9lkf.apps.googleusercontent.com")) // 👈 Replace this
                            .build();

            com.google.api.client.googleapis.auth.oauth2.GoogleIdToken googleIdToken =
                    verifier.verify(idToken);

            if (googleIdToken != null) {
                com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload payload = googleIdToken.getPayload();
                String email = payload.getEmail();
                boolean emailVerified = Boolean.TRUE.equals(payload.getEmailVerified());

                if (emailVerified) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("verified", true);
                    response.put("email", email);
                    return ResponseEntity.ok(response);
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("verified", false, "error", "Email is not verified by Google"));
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("verified", false, "error", "Invalid Google ID token"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("verified", false, "error", e.getMessage()));
        }
    }

}
