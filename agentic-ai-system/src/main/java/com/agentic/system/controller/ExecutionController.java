package com.agentic.system.controller;

import com.agentic.system.core.ExecutionResult;
import com.agentic.system.dto.ApiResponse;
import com.agentic.system.service.ExecutionEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Execution management and monitoring
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/executions")
@CrossOrigin(origins = "*")
public class ExecutionController {
    private final ExecutionEngine executionEngine;

    public ExecutionController(ExecutionEngine executionEngine) {
        this.executionEngine = executionEngine;
    }

    /**
     * Get execution result by task ID
     */
    @GetMapping("/{taskId}")
    public ResponseEntity<ApiResponse<ExecutionResult>> getExecutionResult(@PathVariable String taskId) {
        try {
            ExecutionResult result = executionEngine.getExecutionResult(taskId);
            if (result == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.failure("Execution result not found", "Task ID: " + taskId));
            }
            return ResponseEntity.ok(ApiResponse.success("Execution result retrieved", result));
        } catch (Exception e) {
            log.error("Error retrieving execution result", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure("Error retrieving result", e.getMessage()));
        }
    }

    /**
     * Get all execution results
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ExecutionResult>>> getAllExecutionResults() {
        try {
            List<ExecutionResult> results = executionEngine.getAllExecutionResults();
            return ResponseEntity.ok(ApiResponse.success("Execution results retrieved", results));
        } catch (Exception e) {
            log.error("Error retrieving execution results", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure("Error retrieving results", e.getMessage()));
        }
    }

    /**
     * Get execution statistics
     */
    @GetMapping("/stats/all")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getExecutionStats() {
        try {
            Map<String, Object> stats = executionEngine.getExecutionStats();
            return ResponseEntity.ok(ApiResponse.success("Execution statistics retrieved", stats));
        } catch (Exception e) {
            log.error("Error retrieving execution stats", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure("Error retrieving statistics", e.getMessage()));
        }
    }
}
