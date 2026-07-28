package com.agentic.system.core;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;

/**
 * Represents an autonomous agent in the system
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Agent {
    private String id;
    private String name;
    private String description;
    private AgentType type;
    private AgentStatus status;
    private Map<String, Object> capabilities;
    private Map<String, String> config;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;

    public Agent(String id, String name, String description, AgentType type) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.status = AgentStatus.IDLE;
        this.capabilities = new HashMap<>();
        this.config = new HashMap<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
