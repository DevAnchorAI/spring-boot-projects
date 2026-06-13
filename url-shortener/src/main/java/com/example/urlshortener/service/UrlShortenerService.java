package com.example.urlshortener.service;

import com.example.urlshortener.domain.UrlMapping;
import com.example.urlshortener.repository.UrlMappingRepository;
import com.example.urlshortener.util.Base62;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class UrlShortenerService {

    private final UrlMappingRepository repository;

    public UrlShortenerService(UrlMappingRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public String shorten(String originalUrl, Instant expiresAt) {
        // Optionally, return existing mapping for same originalUrl
        Optional<UrlMapping> existing = repository.findByOriginalUrl(originalUrl);
        if (existing.isPresent()) {
            return existing.get().getShortCode();
        }

        UrlMapping mapping = new UrlMapping(originalUrl);
        mapping.setExpiresAt(expiresAt);

        // Generate initial short code
        String code = generateShortCode();
        mapping.setShortCode(code);

        try {
            // save with initial code
            UrlMapping saved = repository.save(mapping);
            // regenerate code from id for better distribution
            String betterCode = Base62.encode(saved.getId());
            saved.setShortCode(betterCode);
            repository.save(saved);
            return betterCode;
        } catch (DataIntegrityViolationException ex) {
            // rare: collision or constraint violation - fallback: append random suffix and retry a few times
            for (int i = 0; i < 5; i++) {
                String fallback = code + ("" + (int)(Math.random() * 900 + 100));
                mapping.setShortCode(fallback);
                try {
                    repository.save(mapping);
                    return fallback;
                } catch (DataIntegrityViolationException e) {
                    // continue
                }
            }
            throw ex;
        }
    }

    private String generateShortCode() {
        // Generate a temporary code using timestamp and random
        long timestamp = System.currentTimeMillis();
        int random = (int)(Math.random() * 10000);
        return Base62.encode(timestamp * 10000 + random);
    }

    @Transactional(readOnly = true)
    public UrlMapping resolve(String code) {
        Optional<UrlMapping> opt = repository.findByShortCode(code);
        if (opt.isEmpty()) throw new NotFoundException("Short code not found");
        UrlMapping mapping = opt.get();
        if (mapping.getExpiresAt() != null && mapping.getExpiresAt().isBefore(Instant.now())) throw new NotFoundException("Short code expired");
        return mapping;
    }

    @Transactional
    public void incrementClick(String code) {
        UrlMapping mapping = resolve(code);
        mapping.incrementClickCount();
        repository.save(mapping);
    }

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) {
            super(message);
        }
    }
}

