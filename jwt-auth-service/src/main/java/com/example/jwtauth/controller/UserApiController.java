package com.example.jwtauth.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserApiController {

    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> me(Principal principal) {
        Map<String, String> resp = new HashMap<>();
        resp.put("username", principal != null ? principal.getName() : "anonymous");
        resp.put("role", "ROLE_USER");
        return ResponseEntity.ok(resp);
    }
}

