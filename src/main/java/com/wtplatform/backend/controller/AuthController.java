package com.wtplatform.backend.controller;

import com.wtplatform.backend.model.User;
import com.wtplatform.backend.service.AuthService;
import com.wtplatform.backend.dto.AuthResponse;
import com.wtplatform.backend.dto.LoginRequest;
import com.wtplatform.backend.dto.LoginResponse;
import com.wtplatform.backend.dto.RegisterRequest;
import com.wtplatform.backend.dto.ErrorResponse;
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
            
            // Get both token and user role
            AuthResponse authResponse = authService.authenticateUserWithRole(loginRequest.getEmail(), loginRequest.getPassword());
            
            logger.debug("Authentication successful for user: {}, token generated", loginRequest.getEmail());
            logger.debug("Token: {}", authResponse.getToken().substring(0, Math.min(20, authResponse.getToken().length())) + "...");
            
            return ResponseEntity.ok(new LoginResponse(authResponse.getToken(), authResponse.getRole()));
        } catch (Exception e) {
            logger.error("Login failed for user: " + loginRequest.getEmail(), e);
            return ResponseEntity.badRequest().body(new ErrorResponse("Invalid credentials: " + e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        try {
            logger.debug("Register request received for user: {}", registerRequest.getEmail());
            User user = authService.registerUser(
                registerRequest.getEmail(), 
                registerRequest.getPassword(),
                registerRequest.getRole()
            );
            logger.debug("Registration successful for user: {}", registerRequest.getEmail());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            logger.error("Registration failed for user: " + registerRequest.getEmail(), e);
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
} 