package com.agentic.system.core;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;

/**
 * Represents a task to be executed by agents
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Task {
    private String id;
    private String name;
    private String description;
    private TaskType type;
    private TaskStatus status;
    private String agentId;
    private Map<String, Object> input;
    private Map<String, Object> output;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private int priority;
    private int retryCount;
    private int maxRetries;
    private String error;
    private long executionTimeMs;

    public Task(String id, String name, TaskType type, String agentId) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.status = TaskStatus.PENDING;
        this.agentId = agentId;
        this.input = new HashMap<>();
        this.output = new HashMap<>();
        this.createdAt = LocalDateTime.now();
        this.priority = 5; // Medium priority
        this.retryCount = 0;
        this.maxRetries = 3;
        this.executionTimeMs = 0;
    }
}
