package com.example.foodapp.dto;

public class LoginRequest {
    private String email;    // use 'email' instead of 'username'
    private String password;

    public LoginRequest() {}  // default constructor

    public String getEmail() { return email; }   // getter for email
    public void setEmail(String email) { this.email = email; }  // setter

    public String getPassword() { return password; } // getter
    public void setPassword(String password) { this.password = password; } // setter
}
