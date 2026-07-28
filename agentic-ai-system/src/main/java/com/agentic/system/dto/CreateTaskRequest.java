package com.agentic.system.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;

/**
 * DTO for creating a new task
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateTaskRequest {
    @NotBlank(message = "Task name is required")
    private String name;

    private String description;

    @NotBlank(message = "Task type is required")
    private String type;

    @NotBlank(message = "Agent ID is required")
    private String agentId;

    private Map<String, Object> input;

    private int priority = 5;

    private int maxRetries = 3;
}
