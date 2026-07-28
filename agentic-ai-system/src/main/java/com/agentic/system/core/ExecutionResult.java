package com.agentic.system.core;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

/**
 * Represents the result of task execution
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExecutionResult {
    private String taskId;
    private String agentId;
    private boolean success;
    private Map<String, Object> result;
    private String message;
    private String error;
    private LocalDateTime executedAt;
    private long executionTimeMs;
    private Map<String, String> metadata;

    public ExecutionResult(String taskId, String agentId, boolean success) {
        this.taskId = taskId;
        this.agentId = agentId;
        this.success = success;
        this.result = new HashMap<>();
        this.metadata = new HashMap<>();
        this.executedAt = LocalDateTime.now();
        this.executionTimeMs = 0;
    }

    public void putResultEntry(String key, Object value) {
        this.result.put(key, value);
    }

    public void putMetadata(String key, String value) {
        this.metadata.put(key, value);
    }
}
