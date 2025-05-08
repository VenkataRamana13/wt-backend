package com.wtplatform.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        final String requestURI = request.getRequestURI();
        
        // Skip authentication for public endpoints and OPTIONS requests
        if (requestURI.startsWith("/auth/") || request.getMethod().equals("OPTIONS")) {
            logger.debug("Skipping JWT filter for path: " + requestURI + " with method: " + request.getMethod());
            filterChain.doFilter(request, response);
            return;
        }

        logger.debug("Processing request: " + requestURI + " [" + request.getMethod() + "]");
        logger.debug("Authorization header: " + request.getHeader("Authorization"));
        
        try {
            String jwt = parseJwt(request);
            
            if (jwt != null) {
                logger.debug("JWT token found in request");
                String username = jwtUtils.extractUsername(jwt);
                
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    
                    if (jwtUtils.validateToken(jwt, userDetails)) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        
                        // Important: Set the authentication BEFORE continuing the filter chain
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        logger.debug("Authentication set in SecurityContext for user: " + username);
                    } else {
                        logger.warn("JWT token validation failed for token: " + jwt.substring(0, 10) + "...");
                    }
                }
            } else {
                logger.debug("No JWT token found in request");
            }
        } catch (Exception e) {
            logger.error("Cannot process JWT token: " + e.getMessage(), e);
            // Do not throw exception - let the security chain handle it
        }
        
        // Always continue the filter chain
        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        
        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        
        return null;
    }
} 