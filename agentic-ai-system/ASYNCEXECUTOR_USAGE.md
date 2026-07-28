# AsyncExecutor Usage Guide

## Bean Definition

**File**: `AsyncConfig.java`

```java
@Bean(name = "asyncExecutor")
public Executor asyncExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(5);        // Minimum threads
    executor.setMaxPoolSize(10);        // Maximum threads
    executor.setQueueCapacity(50);      // Queue capacity
    executor.setThreadNamePrefix("Async-Executor-");
    executor.initialize();
    return executor;
}
```

**Configuration**:
- Core Pool Size: 5 threads
- Max Pool Size: 10 threads
- Queue Capacity: 50 tasks
- Thread Name: "Async-Executor-1", "Async-Executor-2", etc.

---

## Where AsyncExecutor is Used

### 1. ExecutionEngine.executeTask()

**File**: `ExecutionEngine.java` (Line 52)

```java
@Async("asyncExecutor")
public void executeTask(Task task) {
    // Long-running task execution
    // Executes in asyncExecutor thread pool
    // Updates task status and stores results
}
```

**Purpose**: Execute agent tasks asynchronously without blocking HTTP threads

**Execution Flow**:
1. Worker thread picks task from queue
2. Calls `executeTask()` 
3. Method executes in asyncExecutor thread pool (5-10 threads)
4. Executes based on agent type
5. Stores result in registry
6. Returns immediately to worker thread

---

### 2. ExecutionEngine.handleTaskFailureAsync()

**File**: `ExecutionEngine.java` (Line 157)

```java
@Async("asyncExecutor")
public void handleTaskFailureAsync(Task task, String error) {
    // Handle task failure asynchronously
    // Retry logic runs in separate thread
    // Reschedules task if retries remaining
}
```

**Purpose**: Process task failures and retries asynchronously

**Execution Flow**:
1. Task fails
2. Error handling executes in asyncExecutor thread
3. Increments retry count
4. If retries < maxRetries: reschedules task
5. If retries >= maxRetries: marks as FAILED
6. Logs all events asynchronously

---

## How It Works

### Thread Pool Lifecycle

```
Request → ExecutionEngine.executeTask()
           ↓
    Is @Async("asyncExecutor")?
           ↓
    YES → Take thread from asyncExecutor pool
           ↓
    Execute in separate thread (non-blocking)
           ↓
    Release thread back to pool
           ↓
    Return response immediately
```

### Benefits

✅ **Non-blocking**: HTTP threads are not blocked by long operations
✅ **Scalable**: Can handle multiple concurrent tasks (up to 10)
✅ **Configurable**: Thread pool size adjustable in `AsyncConfig`
✅ **Monitored**: Queue capacity prevents overwhelming the system
✅ **Named threads**: Easy to identify in logs and thread dumps

---

## Usage Example

### Creating an Async Method

```java
@Service
public class YourService {
    
    @Async("asyncExecutor")
    public void longRunningOperation(String data) {
        // This executes in asyncExecutor thread pool
        Thread.sleep(5000);  // Long operation
        log.info("Operation completed");
    }
    
    @Async("asyncExecutor")
    public CompletableFuture<String> asyncOperationWithReturn(String input) {
        return CompletableFuture.completedFuture("Result: " + input);
    }
}
```

### Calling Async Methods

```java
// Method 1: Fire and forget
yourService.longRunningOperation("data");

// Method 2: Wait for result
CompletableFuture<String> future = yourService.asyncOperationWithReturn("input");
String result = future.get();  // Blocking wait
String resultWithTimeout = future.get(10, TimeUnit.SECONDS);
```

---

## Current Usage Summary

| Method | Class | Usage |
|--------|-------|-------|
| `executeTask()` | ExecutionEngine | Execute agent tasks asynchronously |
| `handleTaskFailureAsync()` | ExecutionEngine | Handle task failures and retries |

---

## Thread Pool Monitoring

### View Thread Activity

In application logs, look for threads with prefix `Async-Executor-`:

```
[Async-Executor-1] com.agentic.system.service.ExecutionEngine - Task abc123 executed successfully
[Async-Executor-2] com.agentic.system.service.ExecutionEngine - Task def456 scheduled for retry
```

### Check Queue Status

```bash
curl http://localhost:8080/api/v1/tasks/stats/queue
```

Returns:
```json
{
  "queue_size": 2,
  "pending_count": 1,
  "running_count": 3,
  "completed_count": 25
}
```

---

## Performance Tuning

### Adjust Thread Pool Size

Edit `AsyncConfig.java`:

```java
executor.setCorePoolSize(10);   // Increase minimum threads
executor.setMaxPoolSize(20);    // Increase maximum threads
executor.setQueueCapacity(100); // Increase queue capacity
```

**Guidelines**:
- **CPU-bound tasks**: corePoolSize = number of CPU cores
- **I/O-bound tasks**: corePoolSize = 2 × number of CPU cores
- **Queue capacity**: 3-5 × corePoolSize

### Example for 8-core CPU

```java
// For I/O-heavy operations
executor.setCorePoolSize(16);   // 2 × 8
executor.setMaxPoolSize(32);    // 4 × 8
executor.setQueueCapacity(80);  // 5 × 16
```

---

## Troubleshooting

### Issue: Tasks not executing
**Solution**: Verify `@EnableAsync` is on `AsyncConfig` class

### Issue: Queue full errors
**Solution**: Increase `setQueueCapacity()` or add more executors

### Issue: Thread pool exhausted
**Solution**: Increase `setMaxPoolSize()` or reduce task duration

### Issue: Slow execution
**Solution**: Check thread pool size with:
```bash
grep "Async-Executor" /var/log/app.log | wc -l
```

---

## Summary

✅ **AsyncExecutor Bean** defined in `AsyncConfig.java`
✅ **Used in ExecutionEngine** for task execution
✅ **Configurable thread pool** (5-10 threads)
✅ **Non-blocking execution** via `@Async("asyncExecutor")`
✅ **Proper error handling** with retry logic
✅ **Production-ready** configuration
