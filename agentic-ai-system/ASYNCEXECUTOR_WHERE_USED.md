# AsyncExecutor - Where & How It's Used

## Quick Answer

**The `asyncExecutor` bean is used in `ExecutionEngine.java` for asynchronous task execution.**

---

## Current Usage

### 1. Main Usage: Task Execution

**Location**: `ExecutionEngine.java` → `executeTask()` method (Line 52)

```java
@Async("asyncExecutor")
public void executeTask(Task task) {
    // Executes in asyncExecutor thread pool (5-10 threads)
    // Runs task based on agent type
    // Stores result
    // Updates task status
}
```

**How it works**:
- Worker thread calls `executeTask(task)`
- Spring detects `@Async("asyncExecutor")` annotation
- Method executes in separate thread from asyncExecutor pool
- HTTP request doesn't wait for completion
- Result is stored in registry

---

### 2. Secondary Usage: Async Failure Handling

**Location**: `ExecutionEngine.java` → `handleTaskFailureAsync()` method (Line 144)

```java
@Async("asyncExecutor")
public void handleTaskFailureAsync(Task task, String error) {
    // Handles task failure asynchronously
    // Manages retry logic
    // Reschedules failed tasks
}
```

---

## Thread Pool Configuration

**File**: `AsyncConfig.java`

```
Core Threads: 5 (always active)
Max Threads:  10 (max concurrent tasks)
Queue:        50 (pending tasks)
Thread Names: Async-Executor-1, Async-Executor-2, etc.
```

---

## Flow Diagram

```
HTTP Request
    ↓
AgentController.createTask()
    ↓
TaskService.createTask()
    ↓
TaskQueue.submitTask()
    ↓
ExecutionWorker Thread (polls queue)
    ↓
ExecutionEngine.executeTask()  ← USES asyncExecutor
    ↓
[Async-Executor-1] Thread (from pool)
    ├─ Get agent
    ├─ Execute based on agent type
    ├─ Get result
    ├─ Store result
    └─ Update status
    ↓
Return to thread pool
```

---

## Example Usage in Logs

When task executes, you'll see:

```
[ExecutionWorker] Task 'abc123' picked from queue
[Async-Executor-1] Starting execution for task abc123
[Async-Executor-1] Task abc123 executed successfully by agent xyz
[Async-Executor-1] Execution result stored
```

---

## Why Use asyncExecutor?

✅ **Non-blocking**: HTTP threads continue serving requests
✅ **Scalable**: Handles up to 10 concurrent tasks
✅ **Configurable**: Thread pool size adjustable
✅ **Monitored**: Queue capacity prevents system overload
✅ **Production-ready**: Proper error handling and retry logic

---

## Where to Add More Async Methods

If you need more async operations, add them in services:

```java
@Service
public class YourService {
    
    @Async("asyncExecutor")
    public void asyncOperation() {
        // Will run in asyncExecutor thread pool
    }
}
```

---

## Files Modified

✅ `ExecutionEngine.java` - Now uses `@Async("asyncExecutor")`
✅ `AsyncConfig.java` - Defines the bean
✅ `ASYNCEXECUTOR_USAGE.md` - Created comprehensive guide
