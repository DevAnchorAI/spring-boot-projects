package com.agentic.system.service;

import com.agentic.system.core.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Task Queue for managing task execution
 */
@Slf4j
@Service
public class TaskQueue {
    private final BlockingQueue<Task> queue = new PriorityBlockingQueue<>(100,
        Comparator.comparingInt(Task::getPriority).reversed()
            .thenComparing(Task::getCreatedAt));

    private final Map<String, Task> taskRegistry = new ConcurrentHashMap<>();
    private final ExecutorService executorService;

    public TaskQueue() {
        this.executorService = Executors.newFixedThreadPool(10);
    }

    /**
     * Submit a task to the queue
     */
    public Task submitTask(Task task) {
        task.setStatus(TaskStatus.QUEUED);
        taskRegistry.put(task.getId(), task);
        try {
            queue.put(task);
            log.info("Task {} submitted to queue", task.getId());
        } catch (InterruptedException e) {
            log.error("Interrupted while submitting task: {}", task.getId(), e);
            Thread.currentThread().interrupt();
        }
        return task;
    }

    /**
     * Get the next task from the queue
     */
    public Task getNextTask() {
        try {
            return queue.poll(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("Interrupted while polling queue", e);
            Thread.currentThread().interrupt();
            return null;
        }
    }

    /**
     * Get task by ID
     */
    public Task getTask(String taskId) {
        return taskRegistry.get(taskId);
    }

    /**
     * Get all tasks
     */
    public List<Task> getAllTasks() {
        return new ArrayList<>(taskRegistry.values());
    }

    /**
     * Get tasks by agent
     */
    public List<Task> getTasksByAgent(String agentId) {
        return taskRegistry.values().stream()
                .filter(t -> t.getAgentId().equals(agentId))
                .collect(Collectors.toList());
    }

    /**
     * Get tasks by status
     */
    public List<Task> getTasksByStatus(TaskStatus status) {
        return taskRegistry.values().stream()
                .filter(t -> t.getStatus() == status)
                .collect(Collectors.toList());
    }

    /**
     * Update task status
     */
    public Task updateTaskStatus(String taskId, TaskStatus status) {
        Task task = taskRegistry.get(taskId);
        if (task != null) {
            task.setStatus(status);
            if (status == TaskStatus.RUNNING) {
                task.setStartedAt(java.time.LocalDateTime.now());
            } else if (status == TaskStatus.COMPLETED || status == TaskStatus.FAILED) {
                task.setCompletedAt(java.time.LocalDateTime.now());
                if (task.getStartedAt() != null) {
                    task.setExecutionTimeMs(java.time.temporal.ChronoUnit.MILLIS
                        .between(task.getStartedAt(), task.getCompletedAt()));
                }
            }
            log.info("Task {} status updated to {}", taskId, status);
        }
        return task;
    }

    /**
     * Cancel task
     */
    public boolean cancelTask(String taskId) {
        Task task = taskRegistry.get(taskId);
        if (task != null && task.getStatus() != TaskStatus.COMPLETED
                && task.getStatus() != TaskStatus.FAILED) {
            task.setStatus(TaskStatus.CANCELLED);
            log.info("Task {} cancelled", taskId);
            return true;
        }
        return false;
    }

    /**
     * Get queue size
     */
    public int getQueueSize() {
        return queue.size();
    }

    /**
     * Get pending tasks count
     */
    public long getPendingTasksCount() {
        return taskRegistry.values().stream()
                .filter(t -> t.getStatus() == TaskStatus.PENDING || t.getStatus() == TaskStatus.QUEUED)
                .count();
    }

    /**
     * Shutdown executor
     */
    public void shutdown() {
        executorService.shutdown();
    }
}
