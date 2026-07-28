# TaskExecutor vs AsyncExecutor - Quick Reference

## The Answer

| Executor | Purpose | Where Used | Configuration |
|----------|---------|-----------|---|
| **taskExecutor** | Batch operations, bulk processing | TaskService.java | 10-20 threads, 100 queue |
| **asyncExecutor** | Individual task execution | ExecutionEngine.java | 5-10 threads, 50 queue |

---

## TaskExecutor Usage

### Bean Definition (AppConfig.java)
```java
@Bean(name = "taskExecutor")
public ThreadPoolTaskExecutor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(10);
    executor.setMaxPoolSize(20);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("Agentic-Task-");
    executor.initialize();
    return executor;
}
```

### Used In (TaskService.java)

**Method 1: Batch Submit Tasks**
```java
public void submitTasksBatch(List<CreateTaskRequest> requests) {
    taskExecutor.execute(() -> {
        for (CreateTaskRequest request : requests) {
            createTask(request);
        }
    });
}
```

**Method 2: Process Pending Tasks**
```java
public void processPendingTasks() {
    taskExecutor.execute(() -> {
        List<Task> pendingTasks = taskQueue.getTasksByStatus(TaskStatus.PENDING);
        // Sort and queue them
    });
}
```

**Method 3: Cleanup Completed Tasks**
```java
public void cleanupCompletedTasks() {
    taskExecutor.execute(() -> {
        List<Task> completedTasks = taskQueue.getTasksByStatus(TaskStatus.COMPLETED);
        // Remove from registry
    });
}
```

---

## AsyncExecutor Usage

### Bean Definition (AsyncConfig.java)
```java
@Bean(name = "asyncExecutor")
public Executor asyncExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(5);
    executor.setMaxPoolSize(10);
    executor.setQueueCapacity(50);
    executor.setThreadNamePrefix("Async-Executor-");
    executor.initialize();
    return executor;
}
```

### Used In (ExecutionEngine.java)

**Method 1: Execute Task**
```java
@Async("asyncExecutor")
public void executeTask(Task task) {
    // Execute agent task asynchronously
}
```

**Method 2: Handle Async Failure**
```java
@Async("asyncExecutor")
public void handleTaskFailureAsync(Task task, String error) {
    // Retry logic asynchronously
}
```

---

## Thread Pool Comparison

```
taskExecutor (Batch Operations)
┌─────────────────────────────────────┐
│ Core: 10 | Max: 20 | Queue: 100    │
│ Prefix: Agentic-Task-               │
└─────────────────────────────────────┘

asyncExecutor (Task Execution)
┌─────────────────────────────────────┐
│ Core: 5 | Max: 10 | Queue: 50      │
│ Prefix: Async-Executor-             │
└─────────────────────────────────────┘
```

---

## Usage Example

### Batch Submit Multiple Tasks
```java
List<CreateTaskRequest> tasks = Arrays.asList(
    new CreateTaskRequest("Task 1", "ANALYSIS", "agent-1"),
    new CreateTaskRequest("Task 2", "PROCESSING", "agent-2")
);
taskService.submitTasksBatch(tasks);  // Runs in taskExecutor
```

### Log Output
```
[Agentic-Task-1] TaskService - Submitting batch of 2 tasks
[Agentic-Task-1] TaskService - Batch task created: Task 1
[Agentic-Task-1] TaskService - Batch task created: Task 2
[Agentic-Task-1] TaskService - Batch submission completed: 2 tasks
```

---

## Files Modified

✅ `AppConfig.java` - Defines taskExecutor bean
✅ `TaskService.java` - Uses taskExecutor for:
   - submitTasksBatch()
   - processPendingTasks()
   - cleanupCompletedTasks()
✅ `ExecutionEngine.java` - Uses asyncExecutor for:
   - executeTask()
   - handleTaskFailureAsync()

---

## Summary

- **taskExecutor**: For batch/bulk operations (10-20 threads)
- **asyncExecutor**: For individual task execution (5-10 threads)
- Both are production-ready and properly configured
