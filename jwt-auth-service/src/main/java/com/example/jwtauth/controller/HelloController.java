package com.example.jwtauth.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
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
}

