package com.example.urlshortener.web.dto;

public class ShortenRequest {
    private String url;
    private String expiresAt; // ISO-8601 optional

    public ShortenRequest() {}

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
    }
}

