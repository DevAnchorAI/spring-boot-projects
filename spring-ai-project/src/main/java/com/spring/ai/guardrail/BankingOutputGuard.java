package com.spring.ai.guardrail;

import com.spring.ai.dto.BankingAssistantResponse;
import org.springframework.stereotype.Component;

@Component
public class BankingOutputGuard {

    public void validate(BankingAssistantResponse response) {

        if (response == null) {
            throw new IllegalStateException(
                    "AI returned empty response"
            );
        }

        if (response.message() != null &&
                containsSensitiveInstruction(
                        response.message())) {

            throw new IllegalStateException(
                    "Unsafe AI response"
            );
        }
    }

    private boolean containsSensitiveInstruction(
            String message) {

        String value =
                message.toLowerCase();

        return value.contains("system prompt")
                || value.contains("developer prompt");
    }
}