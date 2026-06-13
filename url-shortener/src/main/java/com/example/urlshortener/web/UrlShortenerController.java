package com.example.urlshortener.web;

import com.example.urlshortener.domain.UrlMapping;
import com.example.urlshortener.service.UrlShortenerService;
import com.example.urlshortener.web.dto.ShortenRequest;
import com.example.urlshortener.web.dto.ShortenResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Optional;

@RestController
public class UrlShortenerController {

    private final UrlShortenerService service;

    public UrlShortenerController(UrlShortenerService service) {
        this.service = service;
    }

    @PostMapping("/api/shorten")
    public ResponseEntity<ShortenResponse> shorten(@RequestBody ShortenRequest req, @RequestHeader(value = "Host", required = false) String host) {
        if (req == null || !StringUtils.hasText(req.getUrl())) {
            return ResponseEntity.badRequest().build();
        }
        Instant expiresAt = null;
        if (StringUtils.hasText(req.getExpiresAt())) {
            try {
                expiresAt = Instant.parse(req.getExpiresAt());
            } catch (DateTimeParseException ex) {
                return ResponseEntity.badRequest().build();
            }
        }

        String code = service.shorten(req.getUrl(), expiresAt);
        String base = "http://" + (host != null ? host : "localhost:8080");
        ShortenResponse resp = new ShortenResponse(code, base + "/" + code, req.getUrl());
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        try {
            UrlMapping mapping = service.resolve(code);
            service.incrementClick(code);
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(mapping.getOriginalUrl()));
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        } catch (UrlShortenerService.NotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/api/info/{code}")
    public ResponseEntity<?> info(@PathVariable String code) {
        try {
            UrlMapping mapping = service.resolve(code);
            return ResponseEntity.ok(mapping);
        } catch (UrlShortenerService.NotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }
}

