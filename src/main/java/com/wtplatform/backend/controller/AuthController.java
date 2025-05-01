package com.wtplatform.backend.controller;

import com.wtplatform.backend.model.User;
import com.wtplatform.backend.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            logger.debug("Login request received for user: {}", loginRequest.getEmail());
            String token = authService.authenticateUser(loginRequest.getEmail(), loginRequest.getPassword());
            logger.debug("Authentication successful for user: {}, token generated", loginRequest.getEmail());
            logger.debug("Token: {}", token.substring(0, Math.min(20, token.length())) + "...");
            return ResponseEntity.ok(new LoginResponse(token));
        } catch (Exception e) {
            logger.error("Login failed for user: " + loginRequest.getEmail(), e);
            return ResponseEntity.badRequest().body(new ErrorResponse("Invalid credentials: " + e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        try {
            logger.debug("Register request received for user: {}", registerRequest.getEmail());
            User user = authService.registerUser(registerRequest.getEmail(), registerRequest.getPassword());
            logger.debug("Registration successful for user: {}", registerRequest.getEmail());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            logger.error("Registration failed for user: " + registerRequest.getEmail(), e);
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
}

class LoginRequest {
    private String email;
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

class RegisterRequest {
    private String email;
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

class LoginResponse {
    private String token;

    public LoginResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}

class ErrorResponse {
    private String message;

    public ErrorResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
} 