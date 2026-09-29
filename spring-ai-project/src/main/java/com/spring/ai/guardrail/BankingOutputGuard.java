package com.spring.ai.guardrail;

import com.spring.ai.dto.BankingAssistantResponse;
import org.springframework.stereotype.Component;

@Component
public class BankingOutputGuard {

    //OUTPUT GUARDRAIL
    public BankingAssistantResponse validateResponse(BankingAssistantResponse response) {

        if (response == null) {
            return new BankingAssistantResponse(null, false, "Unable to generate a valid response.", null);
        }

        /*
         * Never allow success to be null.
         */
        if (response.success() == null) {

            return new BankingAssistantResponse(response.intent(), false, "Unable to generate a valid banking response.", response.data());
        }

        /*
         * Basic protection against accidental
         * system prompt leakage.
         */
        if (containsSensitiveContent(
                response.message())) {

            return new BankingAssistantResponse(response.intent(), false, "Unable to provide the requested response.", null);
        }

        return response;
    }

    /**
     * Basic output leakage detection.
     */
    private boolean containsSensitiveContent(String message) {


        if (message == null) {
            return false;
        }

        String value =  message.toLowerCase();


        return value.contains("system prompt")
                || value.contains("developer prompt")
                || value.contains("developer message")
                || value.contains("api key")
                || value.contains("secret key")
                || value.contains("password");
    }


}