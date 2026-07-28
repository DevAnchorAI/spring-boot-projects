package com.agentic.system.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;

/**
 * DTO for creating a new agent
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateAgentRequest {
    @NotBlank(message = "Agent name is required")
    private String name;

    private String description;

    @NotBlank(message = "Agent type is required")
    private String type;

    private Map<String, Object> capabilities;

    private Map<String, String> config;
}
