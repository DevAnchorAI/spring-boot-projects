package com.bs.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.bs.dto.AuthRequest;
import com.bs.dto.AuthResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class JwtTokenService {

    private final RestTemplate restTemplate;

    @Value("${auth.service.url:http://localhost:8080}")
    private String authServiceUrl;

    public JwtTokenService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Login to JWT Auth Service and get JWT token
     */
    public String login(String username, String password) {
        try {
            String loginUrl = authServiceUrl + "/api/auth/login";

            AuthRequest request = new AuthRequest(username, password);
            ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                    loginUrl,
                    request,
                    AuthResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String token = response.getBody().getToken();
                log.info("Successfully obtained JWT token for user: {}", username);
                return token;
            }
        } catch (Exception e) {
            log.error("Error logging in to JWT Auth Service: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Validate JWT token with Auth Service
     */
    public boolean validateToken(String token) {
        try {
            String validateUrl = authServiceUrl + "/api/auth/validate";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Boolean> response = restTemplate.exchange(
                    validateUrl,
                    HttpMethod.GET,
                    entity,
                    Boolean.class
            );

            return response.getStatusCode().is2xxSuccessful() &&
                   (response.getBody() != null ? response.getBody() : false);
        } catch (Exception e) {
            log.error("Error validating token with JWT Auth Service: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Register new user with Auth Service
     */
    public boolean register(String username, String password) {
        try {
            String registerUrl = authServiceUrl + "/api/auth/register";

            AuthRequest request = new AuthRequest(username, password);
            ResponseEntity<Object> response = restTemplate.postForEntity(
                    registerUrl,
                    request,
                    Object.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully registered user: {}", username);
                return true;
            }
        } catch (Exception e) {
            log.error("Error registering user with JWT Auth Service: {}", e.getMessage());
        }
        return false;
    }
}
