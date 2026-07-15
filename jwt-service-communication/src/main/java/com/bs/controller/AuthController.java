package com.bs.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bs.dto.AuthRequest;
import com.bs.dto.AuthResponse;
import com.bs.service.JwtTokenService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtTokenService jwtTokenService;

    public AuthController(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    /**
     * Login endpoint - communicates with JWT Auth Service
     * Returns JWT token for subsequent requests
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            if (request.getUsername() == null || request.getUsername().isEmpty() ||
                request.getPassword() == null || request.getPassword().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Username and password are required");
            }

            String token = jwtTokenService.login(request.getUsername(), request.getPassword());

            if (token != null) {
                AuthResponse response = new AuthResponse(
                        token,
                        "Login successful",
                        true
                );
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new AuthResponse(
                                null,
                                "Invalid credentials",
                                false
                        ));
            }
        } catch (Exception e) {
            log.error("Error during login: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(
                            null,
                            "Error during login: " + e.getMessage(),
                            false
                    ));
        }
    }

    /**
     * Register endpoint - communicates with JWT Auth Service
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest request) {
        try {
            if (request.getUsername() == null || request.getUsername().isEmpty() ||
                request.getPassword() == null || request.getPassword().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Username and password are required");
            }

            boolean registered = jwtTokenService.register(request.getUsername(), request.getPassword());

            if (registered) {
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(new AuthResponse(
                                null,
                                "Registration successful",
                                true
                        ));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new AuthResponse(
                                null,
                                "Registration failed",
                                false
                        ));
            }
        } catch (Exception e) {
            log.error("Error during registration: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(
                            null,
                            "Error during registration: " + e.getMessage(),
                            false
                    ));
        }
    }
}
