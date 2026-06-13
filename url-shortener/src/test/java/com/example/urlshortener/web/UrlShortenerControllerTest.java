package com.example.urlshortener.web;

import com.example.urlshortener.service.UrlShortenerService;
import com.example.urlshortener.web.dto.ShortenRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UrlShortenerController.class)
class UrlShortenerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UrlShortenerService service;

    @Test
    void testShortenUrlSuccess() throws Exception {
        // Arrange
        ShortenRequest request = new ShortenRequest();
        request.setUrl("https://example.com/very/long/url/to/shorten");

        when(service.shorten("https://example.com/very/long/url/to/shorten", null))
                .thenReturn("abc123");

        // Act & Assert
        mockMvc.perform(post("/api/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Host", "localhost:8080"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("abc123"))
                .andExpect(jsonPath("$.shortUrl").value("http://localhost:8080/abc123"))
                .andExpect(jsonPath("$.originalUrl").value("https://example.com/very/long/url/to/shorten"));
    }

    @Test
    void testShortenUrlWithExpiration() throws Exception {
        // Arrange
        ShortenRequest request = new ShortenRequest();
        request.setUrl("https://example.com/test");
        request.setExpiresAt("2026-12-31T23:59:59Z");

        Instant expiryTime = Instant.parse("2026-12-31T23:59:59Z");
        when(service.shorten("https://example.com/test", expiryTime))
                .thenReturn("xyz789");

        // Act & Assert
        mockMvc.perform(post("/api/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("Host", "localhost:8080"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("xyz789"))
                .andExpect(jsonPath("$.shortUrl").value("http://localhost:8080/xyz789"));
    }

    @Test
    void testShortenUrlWithoutHost() throws Exception {
        // Arrange
        ShortenRequest request = new ShortenRequest();
        request.setUrl("https://github.com/user/repo");

        when(service.shorten("https://github.com/user/repo", null))
                .thenReturn("def456");

        // Act & Assert
        mockMvc.perform(post("/api/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("def456"))
                .andExpect(jsonPath("$.shortUrl").value("http://localhost:8080/def456"));
    }

    @Test
    void testShortenUrlEmptyUrl() throws Exception {
        // Arrange
        ShortenRequest request = new ShortenRequest();
        request.setUrl("");

        // Act & Assert
        mockMvc.perform(post("/api/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testShortenUrlNullUrl() throws Exception {
        // Arrange
        ShortenRequest request = new ShortenRequest();
        request.setUrl(null);

        // Act & Assert
        mockMvc.perform(post("/api/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testShortenUrlNullRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content("null"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testShortenUrlInvalidExpirationFormat() throws Exception {
        // Arrange
        ShortenRequest request = new ShortenRequest();
        request.setUrl("https://example.com/test");
        request.setExpiresAt("invalid-date");

        // Act & Assert
        mockMvc.perform(post("/api/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testShortenUrlWithSpecialCharacters() throws Exception {
        // Arrange
        String specialUrl = "https://example.com/search?q=hello%20world&filter=true&id=123";
        ShortenRequest request = new ShortenRequest();
        request.setUrl(specialUrl);

        when(service.shorten(specialUrl, null))
                .thenReturn("spec999");

        // Act & Assert
        mockMvc.perform(post("/api/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("spec999"))
                .andExpect(jsonPath("$.originalUrl").value(specialUrl));
    }

    @Test
    void testShortenUrlWithLongUrl() throws Exception {
        // Arrange
        String longUrl = "https://example.com/" + "a".repeat(2000);
        ShortenRequest request = new ShortenRequest();
        request.setUrl(longUrl);

        when(service.shorten(longUrl, null))
                .thenReturn("long123");

        // Act & Assert
        mockMvc.perform(post("/api/shorten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("long123"));
    }
}

