package com.agentic.system.service;

import com.agentic.system.core.*;
import com.agentic.system.dto.CreateTaskRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service for managing tasks
 */
@Slf4j
@Service
public class TaskService {
    private final TaskQueue taskQueue;
    private final AgentService agentService;

    public TaskService(TaskQueue taskQueue, AgentService agentService) {
        this.taskQueue = taskQueue;
        this.agentService = agentService;
    }

    /**
     * Create and submit a new task
     */
    public Task createTask(CreateTaskRequest request) {
        // Verify agent exists
        Agent agent = agentService.getAgent(request.getAgentId());
        if (agent == null) {
            throw new IllegalArgumentException("Agent not found: " + request.getAgentId());
        }

        String taskId = UUID.randomUUID().toString();
        TaskType type = TaskType.valueOf(request.getType().toUpperCase());

        Task task = new Task(taskId, request.getName(), type, request.getAgentId());
        task.setDescription(request.getDescription());
        task.setInput(request.getInput() != null ? request.getInput() : new HashMap<>());
        task.setPriority(request.getPriority());
        task.setMaxRetries(request.getMaxRetries());

        // Submit to queue
        taskQueue.submitTask(task);
        log.info("Task created: {} with ID: {}", request.getName(), taskId);

        return task;
    }

    /**
     * Get task by ID
     */
    public Task getTask(String taskId) {
        return taskQueue.getTask(taskId);
    }

    /**
     * Get all tasks
     */
    public List<Task> getAllTasks() {
        return taskQueue.getAllTasks();
    }

    /**
     * Get tasks by agent
     */
    public List<Task> getTasksByAgent(String agentId) {
        return taskQueue.getTasksByAgent(agentId);
    }

    /**
     * Get tasks by status
     */
    public List<Task> getTasksByStatus(TaskStatus status) {
        return taskQueue.getTasksByStatus(status);
    }

    /**
     * Cancel task
     */
    public boolean cancelTask(String taskId) {
        return taskQueue.cancelTask(taskId);
    }

    /**
     * Get task queue statistics
     */
    public Map<String, Object> getQueueStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("queue_size", taskQueue.getQueueSize());
        stats.put("pending_count", taskQueue.getPendingTasksCount());
        stats.put("completed_count", taskQueue.getTasksByStatus(TaskStatus.COMPLETED).size());
        stats.put("failed_count", taskQueue.getTasksByStatus(TaskStatus.FAILED).size());
        stats.put("running_count", taskQueue.getTasksByStatus(TaskStatus.RUNNING).size());
        return stats;
    }
}
