package com.spring.ai.dto;

import java.util.List;

public record DocumentQAResponse(String answer, List<String> sources) {
}
