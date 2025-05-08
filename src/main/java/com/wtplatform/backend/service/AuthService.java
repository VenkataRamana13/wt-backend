package com.wtplatform.backend.service;

import com.wtplatform.backend.model.User;
import com.wtplatform.backend.repository.UserRepository;
import com.wtplatform.backend.security.JwtUtils;
import com.wtplatform.backend.dto.AuthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    public String authenticateUser(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return jwtUtils.generateToken((User) authentication.getPrincipal());
    }

    public AuthResponse authenticateUserWithRole(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        User user = (User) authentication.getPrincipal();
        String token = jwtUtils.generateToken(user);
        
        return new AuthResponse(token, user.getRole());
    }

    public User registerUser(String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email is already in use!");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        return userRepository.save(user);
    }

    public User registerUser(String email, String password, String role) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email is already in use!");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        
        // Validate and set role
        String normalizedRole = validateAndNormalizeRole(role);
        user.setRole(normalizedRole);
        
        return userRepository.save(user);
    }
    
    private String validateAndNormalizeRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            return "CLIENT"; // Default role
        }
        
        String normalizedRole = role.trim().toUpperCase();
        // Validate that role is one of the allowed values
        if (normalizedRole.equals("ADMIN") || 
            normalizedRole.equals("ADVISOR") || 
            normalizedRole.equals("CLIENT")) {
            return normalizedRole;
        } else {
            return "CLIENT"; // Default to CLIENT if invalid role provided
        }
    }
} 