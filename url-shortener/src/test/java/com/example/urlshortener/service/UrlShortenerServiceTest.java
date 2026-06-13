package com.example.urlshortener.service;

import com.example.urlshortener.domain.UrlMapping;
import com.example.urlshortener.repository.UrlMappingRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UrlShortenerServiceTest {

    @Test
    void shortenCreatesCode() {
        UrlMappingRepository repo = mock(UrlMappingRepository.class);
        UrlShortenerService service = new UrlShortenerService(repo);

        when(repo.findByOriginalUrl(anyString())).thenReturn(Optional.empty());
        // when saving first time, simulate generated id
        ArgumentCaptor<UrlMapping> captor = ArgumentCaptor.forClass(UrlMapping.class);
        when(repo.save(any(UrlMapping.class))).thenAnswer(invocation -> {
            UrlMapping m = invocation.getArgument(0);
            if (m.getId() == null) {
                m.setId(123L);
            }
            return m;
        });

        String code = service.shorten("https://example.com/long/path", null);
        assertNotNull(code);
        assertTrue(code.length() > 0);
        verify(repo, atLeastOnce()).save(captor.capture());
        UrlMapping saved = captor.getValue();
        assertEquals("https://example.com/long/path", saved.getOriginalUrl());
    }

    @Test
    void resolveNotFound() {
        UrlMappingRepository repo = mock(UrlMappingRepository.class);
        UrlShortenerService service = new UrlShortenerService(repo);
        when(repo.findByShortCode("abc")).thenReturn(Optional.empty());
        assertThrows(UrlShortenerService.NotFoundException.class, () -> service.resolve("abc"));
    }
}

