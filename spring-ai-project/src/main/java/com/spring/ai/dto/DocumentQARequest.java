package com.spring.ai.dto;

public record DocumentQARequest(String question,String documentType,String category,String source) {
}
