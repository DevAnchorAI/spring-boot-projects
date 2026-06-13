package com.example.urlshortener.integration;

import com.example.urlshortener.web.dto.ShortenRequest;
import com.example.urlshortener.web.dto.ShortenResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UrlShortenerIntegrationTest {

    @LocalServerPort
    private int port;

    private final TestRestTemplate rest = new TestRestTemplate();

    @Test
    void shortenAndRedirect() {
        String base = "http://localhost:" + port;
        ShortenRequest req = new ShortenRequest();
        req.setUrl("https://example.com/test/1");

        ResponseEntity<ShortenResponse> resp = rest.postForEntity(base + "/api/shorten", req, ShortenResponse.class);
        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        assertNotNull(resp.getBody());
        String shortUrl = resp.getBody().getShortUrl();
        assertTrue(shortUrl.contains("/"));
        // extract code
        String code = shortUrl.substring(shortUrl.lastIndexOf('/') + 1);

        // follow redirect
        ResponseEntity<String> redirect = rest.getForEntity(base + "/" + code, String.class);
        assertTrue(redirect.getStatusCode().is3xxRedirection());
        assertNotNull(redirect.getHeaders().getLocation());
        assertEquals("https://example.com/test/1", redirect.getHeaders().getLocation().toString());

        // info
        ResponseEntity<String> info = rest.getForEntity(base + "/api/info/" + code, String.class);
        assertEquals(HttpStatus.OK, info.getStatusCode());
        assertNotNull(info.getBody());
        assertTrue(info.getBody().contains("originalUrl"));
    }
}

