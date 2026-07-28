package com.agentic.system.service;

import com.agentic.system.core.*;
import com.agentic.system.dto.CreateTaskRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
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
    private final ThreadPoolTaskExecutor taskExecutor;

    public TaskService(TaskQueue taskQueue, AgentService agentService,
                      ThreadPoolTaskExecutor taskExecutor) {
        this.taskQueue = taskQueue;
        this.agentService = agentService;
        this.taskExecutor = taskExecutor;
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

    /**
     * Submit multiple tasks in batch using taskExecutor
     * Useful for bulk task creation
     */
    public void submitTasksBatch(List<CreateTaskRequest> requests) {
        log.info("Submitting batch of {} tasks", requests.size());

        // Execute batch submission in taskExecutor thread pool
        taskExecutor.execute(() -> {
            for (CreateTaskRequest request : requests) {
                try {
                    createTask(request);
                    log.debug("Batch task created: {}", request.getName());
                } catch (Exception e) {
                    log.error("Error creating batch task: {}", request.getName(), e);
                }
            }
            log.info("Batch submission completed: {} tasks", requests.size());
        });
    }

    /**
     * Process pending tasks using taskExecutor
     * Handles high-priority tasks first
     */
    public void processPendingTasks() {
        taskExecutor.execute(() -> {
            try {
                List<Task> pendingTasks = taskQueue.getTasksByStatus(TaskStatus.PENDING);
                log.info("Processing {} pending tasks", pendingTasks.size());

                // Sort by priority (higher priority first)
                pendingTasks.sort((t1, t2) -> Integer.compare(t2.getPriority(), t1.getPriority()));

                for (Task task : pendingTasks) {
                    taskQueue.updateTaskStatus(task.getId(), TaskStatus.QUEUED);
                    log.debug("Queued task: {} with priority: {}", task.getId(), task.getPriority());
                }

                log.info("Pending task processing completed");
            } catch (Exception e) {
                log.error("Error processing pending tasks", e);
            }
        });
    }

    /**
     * Clean up completed tasks using taskExecutor
     * Removes old completed tasks from registry
     */
    public void cleanupCompletedTasks() {
        taskExecutor.execute(() -> {
            try {
                List<Task> completedTasks = taskQueue.getTasksByStatus(TaskStatus.COMPLETED);
                log.info("Cleaning up {} completed tasks", completedTasks.size());

                for (Task task : completedTasks) {
                    taskQueue.cancelTask(task.getId());
                }

                log.info("Task cleanup completed, removed {} tasks", completedTasks.size());
            } catch (Exception e) {
                log.error("Error cleaning up tasks", e);
            }
        });
    }
}
