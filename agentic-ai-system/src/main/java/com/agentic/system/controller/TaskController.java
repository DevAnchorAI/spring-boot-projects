package com.agentic.system.controller;

import com.agentic.system.core.*;
import com.agentic.system.dto.ApiResponse;
import com.agentic.system.dto.CreateTaskRequest;
import com.agentic.system.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Task management
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/tasks")
@CrossOrigin(origins = "*")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Create and submit a new task
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Task>> createTask(@Valid @RequestBody CreateTaskRequest request) {
        try {
            Task task = taskService.createTask(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Task created and queued successfully", task));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.failure("Invalid task request", e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating task", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure("Failed to create task", e.getMessage()));
        }
    }

    /**
     * Get task by ID
     */
    @GetMapping("/{taskId}")
    public ResponseEntity<ApiResponse<Task>> getTask(@PathVariable String taskId) {
        try {
            Task task = taskService.getTask(taskId);
            if (task == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.failure("Task not found", "Task ID: " + taskId));
            }
            return ResponseEntity.ok(ApiResponse.success("Task retrieved successfully", task));
        } catch (Exception e) {
            log.error("Error retrieving task", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure("Error retrieving task", e.getMessage()));
        }
    }

    /**
     * Get all tasks
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Task>>> getAllTasks() {
        try {
            List<Task> tasks = taskService.getAllTasks();
            return ResponseEntity.ok(ApiResponse.success("Tasks retrieved successfully", tasks));
        } catch (Exception e) {
            log.error("Error retrieving tasks", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure("Error retrieving tasks", e.getMessage()));
        }
    }

    /**
     * Get tasks by agent ID
     */
    @GetMapping("/agent/{agentId}")
    public ResponseEntity<ApiResponse<List<Task>>> getTasksByAgent(@PathVariable String agentId) {
        try {
            List<Task> tasks = taskService.getTasksByAgent(agentId);
            return ResponseEntity.ok(ApiResponse.success("Tasks retrieved successfully", tasks));
        } catch (Exception e) {
            log.error("Error retrieving tasks by agent", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure("Error retrieving tasks", e.getMessage()));
        }
    }

    /**
     * Get tasks by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<Task>>> getTasksByStatus(@PathVariable String status) {
        try {
            TaskStatus taskStatus = TaskStatus.valueOf(status.toUpperCase());
            List<Task> tasks = taskService.getTasksByStatus(taskStatus);
            return ResponseEntity.ok(ApiResponse.success("Tasks retrieved successfully", tasks));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.failure("Invalid task status", e.getMessage()));
        } catch (Exception e) {
            log.error("Error retrieving tasks by status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure("Error retrieving tasks", e.getMessage()));
        }
    }

    /**
     * Cancel a task
     */
    @PutMapping("/{taskId}/cancel")
    public ResponseEntity<ApiResponse<String>> cancelTask(@PathVariable String taskId) {
        try {
            boolean cancelled = taskService.cancelTask(taskId);
            if (!cancelled) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.failure("Cannot cancel task",
                                "Task either doesn't exist or is already completed/failed"));
            }
            return ResponseEntity.ok(ApiResponse.success("Task cancelled successfully", null));
        } catch (Exception e) {
            log.error("Error cancelling task", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure("Error cancelling task", e.getMessage()));
        }
    }

    /**
     * Get queue statistics
     */
    @GetMapping("/stats/queue")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getQueueStats() {
        try {
            Map<String, Object> stats = taskService.getQueueStats();
            return ResponseEntity.ok(ApiResponse.success("Queue statistics retrieved", stats));
        } catch (Exception e) {
            log.error("Error retrieving queue stats", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure("Error retrieving statistics", e.getMessage()));
        }
    }
}
