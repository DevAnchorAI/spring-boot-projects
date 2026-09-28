package com.spring.ai.dto;


import org.apache.poi.ss.formula.functions.T;

public record BankingAssistantResponse<T>(
        BankingIntent intent,
        Boolean success,
        String message,
        T data
) {
}