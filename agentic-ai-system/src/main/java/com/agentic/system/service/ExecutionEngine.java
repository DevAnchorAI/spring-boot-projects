package com.agentic.system.service;

import com.agentic.system.core.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

/**
 * Execution engine for executing tasks via agents
 */
@Slf4j
@Service
public class ExecutionEngine {
    private final AgentService agentService;
    private final TaskQueue taskQueue;
    private final Map<String, ExecutionResult> resultRegistry = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

    public ExecutionEngine(AgentService agentService, TaskQueue taskQueue) {
        this.agentService = agentService;
        this.taskQueue = taskQueue;
        startExecutionWorker();
    }

    /**
     * Start the execution worker thread
     */
    private void startExecutionWorker() {
        Thread workerThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Task task = taskQueue.getNextTask();
                    if (task != null) {
                        executeTask(task);
                    }
                } catch (Exception e) {
                    log.error("Error in execution worker", e);
                }
            }
        }, "ExecutionWorker");

        workerThread.setDaemon(true);
        workerThread.start();
    }

    /**
     * Execute a task
     */
    @Async
    public void executeTask(Task task) {
        Agent agent = agentService.getAgent(task.getAgentId());
        if (agent == null) {
            task.setStatus(TaskStatus.FAILED);
            task.setError("Agent not found");
            taskQueue.updateTaskStatus(task.getId(), TaskStatus.FAILED);
            return;
        }

        try {
            taskQueue.updateTaskStatus(task.getId(), TaskStatus.RUNNING);
            agentService.updateAgentStatus(agent.getId(), AgentStatus.RUNNING);

            // Simulate task execution based on agent type
            ExecutionResult result = executeByAgentType(agent, task);

            if (result.isSuccess()) {
                task.setOutput(result.getResult());
                taskQueue.updateTaskStatus(task.getId(), TaskStatus.COMPLETED);
            } else {
                handleTaskFailure(task, result.getError());
            }

            resultRegistry.put(task.getId(), result);
            log.info("Task {} executed successfully by agent {}", task.getId(), agent.getId());

        } catch (Exception e) {
            log.error("Error executing task: {}", task.getId(), e);
            handleTaskFailure(task, e.getMessage());
        } finally {
            agentService.updateAgentStatus(agent.getId(), AgentStatus.IDLE);
        }
    }

    /**
     * Execute task based on agent type
     */
    private ExecutionResult executeByAgentType(Agent agent, Task task) {
        ExecutionResult result = new ExecutionResult(task.getId(), agent.getId(), true);
        result.setMessage("Task executed successfully");

        switch (agent.getType()) {
            case ANALYZER:
                result.putResultEntry("analysis_type", "code");
                result.putResultEntry("status", "analyzed");
                result.putResultEntry("issues_found", 0);
                result.setMessage("Code analysis completed");
                break;

            case PROCESSOR:
                result.putResultEntry("records_processed",
                    ((Map<String, Object>) task.getInput()).getOrDefault("batch_size", 100));
                result.putResultEntry("status", "processed");
                result.setMessage("Data processing completed");
                break;

            case VALIDATOR:
                result.putResultEntry("validation_passed", true);
                result.putResultEntry("status", "validated");
                result.setMessage("Validation completed successfully");
                break;

            case EXECUTOR:
                result.putResultEntry("execution_status", "completed");
                result.putResultEntry("status", "executed");
                result.setMessage("Task execution completed");
                break;

            case MONITOR:
                result.putResultEntry("monitoring_status", "active");
                result.putResultEntry("status", "monitoring");
                result.setMessage("Monitoring task completed");
                break;

            case INTEGRATOR:
                result.putResultEntry("integration_status", "completed");
                result.putResultEntry("status", "integrated");
                result.setMessage("Integration task completed");
                break;

            default:
                result.putResultEntry("status", "completed");
        }

        return result;
    }

    /**
     * Handle task failure with retry logic
     */
    private void handleTaskFailure(Task task, String error) {
        task.setError(error);
        task.setRetryCount(task.getRetryCount() + 1);

        if (task.getRetryCount() < task.getMaxRetries()) {
            task.setStatus(TaskStatus.RETRY);
            taskQueue.updateTaskStatus(task.getId(), TaskStatus.RETRY);

            // Reschedule with delay
            scheduler.schedule(() -> {
                task.setStatus(TaskStatus.QUEUED);
                taskQueue.submitTask(task);
            }, 5, TimeUnit.SECONDS);

            log.info("Task {} scheduled for retry ({}/{})",
                task.getId(), task.getRetryCount(), task.getMaxRetries());
        } else {
            task.setStatus(TaskStatus.FAILED);
            taskQueue.updateTaskStatus(task.getId(), TaskStatus.FAILED);
            log.error("Task {} failed after {} retries", task.getId(), task.getMaxRetries());
        }
    }

    /**
     * Get execution result
     */
    public ExecutionResult getExecutionResult(String taskId) {
        return resultRegistry.get(taskId);
    }

    /**
     * Get all execution results
     */
    public List<ExecutionResult> getAllExecutionResults() {
        return new ArrayList<>(resultRegistry.values());
    }

    /**
     * Get execution statistics
     */
    public Map<String, Object> getExecutionStats() {
        Map<String, Object> stats = new HashMap<>();
        long successCount = resultRegistry.values().stream()
                .filter(ExecutionResult::isSuccess)
                .count();
        stats.put("total_executions", resultRegistry.size());
        stats.put("successful", successCount);
        stats.put("failed", resultRegistry.size() - successCount);
        return stats;
    }
}
