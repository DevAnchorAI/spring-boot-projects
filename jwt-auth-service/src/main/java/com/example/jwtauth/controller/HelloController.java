package com.example.jwtauth.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HelloController {

    @GetMapping("/hello")
    public ResponseEntity<Map<String, String>> hello(Principal principal) {
        Map<String, String> resp = new HashMap<>();
        String name = (principal != null) ? principal.getName() : "anonymous";
        resp.put("message", "Hello, " + name + "!");
        resp.put("status", "ok");
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/getUserDetails")
    public ResponseEntity<Map<String, Object>> hello() {
        Map<String, Object> resp = new HashMap<>();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object obj = authentication.getPrincipal();
        User user = (User)obj;
        resp.put("user", user);
        resp.put("status", "ok");
        return ResponseEntity.ok(resp);
    }
}

