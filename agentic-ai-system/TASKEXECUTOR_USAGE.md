# TaskExecutor - Where & How It's Used

## Quick Answer

**The `taskExecutor` bean is now used in `TaskService.java` for batch task processing.**

---

## Current Usage

### 1. Batch Task Submission

**Location**: `TaskService.java` → `submitTasksBatch()` method

```java
public void submitTasksBatch(List<CreateTaskRequest> requests) {
    // Execute batch submission in taskExecutor thread pool
    taskExecutor.execute(() -> {
        for (CreateTaskRequest request : requests) {
            createTask(request);
        }
    });
}
```

**Purpose**: Submit multiple tasks in parallel without blocking HTTP request

**Usage Example**:
```bash
POST /api/v1/tasks/batch
[
  {"name": "Task 1", "type": "ANALYSIS", "agentId": "agent-1"},
  {"name": "Task 2", "type": "PROCESSING", "agentId": "agent-2"}
]
```

---

### 2. Process Pending Tasks

**Location**: `TaskService.java` → `processPendingTasks()` method

```java
public void processPendingTasks() {
    taskExecutor.execute(() -> {
        List<Task> pendingTasks = taskQueue.getTasksByStatus(TaskStatus.PENDING);
        // Sort by priority and queue them
    });
}
```

**Purpose**: Handle high-priority pending tasks in background

**Execution Flow**:
1. Get all pending tasks
2. Sort by priority (highest first)
3. Queue for execution
4. All in taskExecutor thread pool (10-20 threads)

---

### 3. Cleanup Completed Tasks

**Location**: `TaskService.java` → `cleanupCompletedTasks()` method

```java
public void cleanupCompletedTasks() {
    taskExecutor.execute(() -> {
        List<Task> completedTasks = taskQueue.getTasksByStatus(TaskStatus.COMPLETED);
        // Remove old completed tasks
    });
}
```

**Purpose**: Clean up registry asynchronously without blocking requests

---

## Thread Pool Configuration

**File**: `AppConfig.java`

```java
@Bean(name = "taskExecutor")
public ThreadPoolTaskExecutor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(10);           // Minimum threads
    executor.setMaxPoolSize(20);            // Maximum threads
    executor.setQueueCapacity(100);         // Queue capacity
    executor.setThreadNamePrefix("Agentic-Task-");
    executor.initialize();
    return executor;
}
```

**Configuration Details**:
- **Core Pool Size**: 10 threads (always active)
- **Max Pool Size**: 20 threads (max concurrent batch operations)
- **Queue Capacity**: 100 batch jobs
- **Thread Naming**: "Agentic-Task-1", "Agentic-Task-2", etc.

---

## How TaskExecutor Works

```
HTTP Request (POST /api/v1/tasks/batch)
    ↓
TaskService.submitTasksBatch()
    ↓
taskExecutor.execute(() -> { ... })
    ↓
[Agentic-Task-1] Thread (from pool)
    ├─ Loop through all requests
    ├─ Create each task
    ├─ Submit to queue
    └─ Log completion
    ↓
Return HTTP 202 ACCEPTED immediately
```

**Benefits**:
- ✅ Non-blocking batch submission
- ✅ Handles multiple requests concurrently
- ✅ Prevents HTTP timeout on large batches
- ✅ Scalable up to 20 concurrent batch operations

---

## Usage in Code

### Direct Usage

```java
@Service
public class TaskService {
    private final ThreadPoolTaskExecutor taskExecutor;
    
    public TaskService(ThreadPoolTaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }
    
    public void submitTasksBatch(List<CreateTaskRequest> requests) {
        taskExecutor.execute(() -> {
            // Batch processing logic
        });
    }
}
```

### In Other Services

To use taskExecutor in other services:

```java
@Service
public class YourService {
    
    private final ThreadPoolTaskExecutor taskExecutor;
    
    public YourService(ThreadPoolTaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }
    
    public void heavyOperation() {
        taskExecutor.execute(() -> {
            // Long-running operation
        });
    }
}
```

---

## Thread Pool Monitoring

### View in Logs

Look for threads with prefix `Agentic-Task-`:

```
[Agentic-Task-1] TaskService - Submitting batch of 5 tasks
[Agentic-Task-1] TaskService - Batch task created: Analyze Project
[Agentic-Task-2] TaskService - Processing 10 pending tasks
[Agentic-Task-1] TaskService - Batch submission completed: 5 tasks
```

### Check Available Threads

The taskExecutor is separate from asyncExecutor:

| Executor | Core | Max | Queue | Purpose |
|----------|------|-----|-------|---------|
| taskExecutor | 10 | 20 | 100 | Batch operations, cleanup |
| asyncExecutor | 5 | 10 | 50 | Async task execution |

---

## Comparison: taskExecutor vs asyncExecutor

| Feature | taskExecutor | asyncExecutor |
|---------|--------------|---------------|
| Purpose | Batch operations | Task execution |
| Core Threads | 10 | 5 |
| Max Threads | 20 | 10 |
| Queue Capacity | 100 | 50 |
| Used in | TaskService | ExecutionEngine |
| Use Case | Bulk submissions, cleanup | Individual task execution |

---

## Best Practices

### ✅ When to Use taskExecutor

1. **Batch operations**: Multiple task submissions
2. **Bulk processing**: Large dataset handling
3. **Cleanup operations**: Registry maintenance
4. **High-volume requests**: Many concurrent requests

### ❌ When NOT to Use taskExecutor

1. Single task submission → Use HTTP handler directly
2. Time-critical operations → taskExecutor adds overhead
3. Long-running operations (>1 min) → Use asyncExecutor instead

---

## Configuration Tuning

### For CPU-bound Batch Jobs

```java
executor.setCorePoolSize(8);      // Equal to CPU cores
executor.setMaxPoolSize(16);      // 2x CPU cores
executor.setQueueCapacity(80);    // 5x core size
```

### For I/O-bound Batch Jobs

```java
executor.setCorePoolSize(16);     // 2x CPU cores
executor.setMaxPoolSize(32);      // 4x CPU cores
executor.setQueueCapacity(160);   // 5x core size
```

---

## Summary

✅ **taskExecutor** is a thread pool for batch and bulk operations
✅ **Located in** `AppConfig.java`
✅ **Used in** `TaskService.java` for:
   - Batch task submission
   - Pending task processing
   - Completed task cleanup
✅ **Configuration**: 10-20 threads, 100 queue capacity
✅ **Benefits**: Non-blocking, scalable, production-ready
