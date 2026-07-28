# Agentic AI System - Developer Guide

## Overview

This guide is for developers who want to understand, extend, or contribute to the Agentic AI System.

## Architecture Overview

### Layered Architecture

```
┌─────────────────────────────────────────────────────────┐
│                   REST API Layer                        │
│              (Controllers - @RestController)            │
│  AgentController | TaskController | ExecutionController │
└────────────┬────────────────────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────────────────────┐
│                   Service Layer                         │
│              (@Service, @Component)                     │
│  AgentService | TaskService | TaskQueue | ExecutionEngine
└────────────┬────────────────────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────────────────────┐
│                   Model Layer                           │
│         (Domain Models - @Getter @Setter)              │
│  Agent | Task | ExecutionResult | Status Enums        │
└──────────────────────────────────────────────────────────┘
```

## File Organization

### Controllers (REST API Layer)

**Location**: `src/main/java/com/agentic/system/controller/`

Each controller handles specific REST endpoints:

```
AgentController
├── POST /api/v1/agents
├── GET /api/v1/agents
├── GET /api/v1/agents/{id}
├── GET /api/v1/agents/type/{type}
├── GET /api/v1/agents/{id}/status
├── PUT /api/v1/agents/{id}/status
└── DELETE /api/v1/agents/{id}

TaskController
├── POST /api/v1/tasks
├── GET /api/v1/tasks
├── GET /api/v1/tasks/{id}
├── GET /api/v1/tasks/agent/{agentId}
├── GET /api/v1/tasks/status/{status}
├── PUT /api/v1/tasks/{id}/cancel
└── GET /api/v1/tasks/stats/queue

ExecutionController
├── GET /api/v1/executions/{taskId}
├── GET /api/v1/executions
└── GET /api/v1/executions/stats/all
```

### Services (Business Logic Layer)

**Location**: `src/main/java/com/agentic/system/service/`

#### AgentService
- Manages agent lifecycle
- Maintains agent registry (in-memory)
- Creates default agents on startup
- Provides agent queries and updates

```java
public class AgentService {
    // Core methods
    - createAgent(CreateAgentRequest request)
    - getAgent(String agentId)
    - getAllAgents()
    - getAgentsByType(AgentType type)
    - updateAgentStatus(String agentId, AgentStatus status)
    - deleteAgent(String agentId)
    - getAgentStatus(String agentId)
    - getActiveAgents()
}
```

#### TaskService
- High-level task management
- Delegates to TaskQueue for storage
- Provides task queries
- Manages task lifecycle

```java
public class TaskService {
    // Core methods
    - createTask(CreateTaskRequest request)
    - getTask(String taskId)
    - getAllTasks()
    - getTasksByAgent(String agentId)
    - getTasksByStatus(TaskStatus status)
    - cancelTask(String taskId)
    - getQueueStats()
}
```

#### TaskQueue
- Priority-based blocking queue
- Manages task submission and retrieval
- Tracks task registry
- Provides queue statistics

```java
public class TaskQueue {
    // Core methods
    - submitTask(Task task)
    - getNextTask()
    - getTask(String taskId)
    - getAllTasks()
    - getTasksByAgent(String agentId)
    - getTasksByStatus(TaskStatus status)
    - updateTaskStatus(String taskId, TaskStatus status)
    - cancelTask(String taskId)
    - getQueueSize()
    - getPendingTasksCount()
}
```

#### ExecutionEngine
- Asynchronous task execution
- Implements retry logic
- Stores execution results
- Provides execution statistics

```java
public class ExecutionEngine {
    // Core methods
    - executeTask(Task task)
    - executeByAgentType(Agent agent, Task task)
    - handleTaskFailure(Task task, String error)
    - getExecutionResult(String taskId)
    - getAllExecutionResults()
    - getExecutionStats()
}
```

### Models (Domain Layer)

**Location**: `src/main/java/com/agentic/system/core/`

#### Core Entities

```java
Agent {
    String id
    String name
    String description
    AgentType type
    AgentStatus status
    Map<String, Object> capabilities
    Map<String, String> config
    LocalDateTime createdAt
    LocalDateTime updatedAt
    String createdBy
}

Task {
    String id
    String name
    String description
    TaskType type
    TaskStatus status
    String agentId
    Map<String, Object> input
    Map<String, Object> output
    LocalDateTime createdAt
    LocalDateTime startedAt
    LocalDateTime completedAt
    int priority
    int retryCount
    int maxRetries
    String error
    long executionTimeMs
}

ExecutionResult {
    String taskId
    String agentId
    boolean success
    Map<String, Object> result
    String message
    String error
    LocalDateTime executedAt
    long executionTimeMs
    Map<String, String> metadata
}
```

### DTOs (Data Transfer)

**Location**: `src/main/java/com/agentic/system/dto/`

```java
ApiResponse<T> {
    boolean success
    String message
    T data
    String error
    long timestamp
    // Factory methods: success(), failure()
}

CreateAgentRequest {
    @NotBlank String name
    String description
    @NotBlank String type
    Map<String, Object> capabilities
    Map<String, String> config
}

CreateTaskRequest {
    @NotBlank String name
    String description
    @NotBlank String type
    @NotBlank String agentId
    Map<String, Object> input
    int priority = 5
    int maxRetries = 3
}
```

## Data Flow

### Creating an Agent

```
HTTP POST /api/v1/agents
    ↓
AgentController.createAgent(CreateAgentRequest)
    ↓
AgentService.createAgent(request)
    ├─> Generate UUID for agent ID
    ├─> Parse AgentType from string
    ├─> Create Agent instance
    ├─> Store in agentRegistry (ConcurrentHashMap)
    └─> Log creation
    ↓
Return ApiResponse<Agent> with HTTP 201
```

### Submitting a Task

```
HTTP POST /api/v1/tasks
    ↓
TaskController.createTask(CreateTaskRequest)
    ↓
TaskService.createTask(request)
    ├─> Verify agent exists
    ├─> Generate UUID for task ID
    ├─> Create Task instance
    └─> Call taskQueue.submitTask(task)
        ├─> Set status to QUEUED
        ├─> Add to taskRegistry
        └─> Put into BlockingQueue (priority-sorted)
    ↓
Return ApiResponse<Task> with HTTP 201
    ↓
ExecutionEngine (in separate thread)
    ├─> Polls queue for tasks
    ├─> Picks next highest priority task
    ├─> Executes task asynchronously
    ├─> Stores execution result
    └─> Updates task status
```

## Key Design Patterns

### 1. Dependency Injection
- Spring's @Autowired for constructor injection
- Promotes loose coupling
- Facilitates testing

```java
@RestController
public class TaskController {
    private final TaskService taskService;
    
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }
}
```

### 2. Service Locator Pattern
- Services manage internal registries
- ConcurrentHashMap for thread-safe access
- Provides filtering and querying

```java
private final Map<String, Agent> agentRegistry = new ConcurrentHashMap<>();
```

### 3. Queue Pattern
- BlockingQueue for task management
- Priority ordering
- FIFO retrieval

```java
private final BlockingQueue<Task> queue = 
    new PriorityBlockingQueue<>(100, 
        Comparator.comparingInt(Task::getPriority).reversed());
```

### 4. Async Execution Pattern
- @Async for non-blocking execution
- ThreadPoolTaskExecutor for concurrency
- Results stored in registry

```java
@Async
public void executeTask(Task task) {
    // Long-running task
}
```

### 5. Standardized Response Pattern
- Consistent ApiResponse wrapper
- Factory methods for responses
- Unified error handling

```java
return ResponseEntity.ok(
    ApiResponse.success("Message", data)
);
```

## Extending the System

### Adding a New Agent Type

1. **Define Agent in Database/Registry**
   ```java
   Agent agent = new Agent(
       UUID.randomUUID().toString(),
       "Custom Analyzer",
       "Performs custom analysis",
       AgentType.CUSTOM
   );
   agentRegistry.put(agent.getId(), agent);
   ```

2. **Implement Execution Logic**
   ```java
   private ExecutionResult executeByAgentType(Agent agent, Task task) {
       if (agent.getType() == AgentType.CUSTOM) {
           // Custom execution logic
           result.putResultEntry("custom_key", "custom_value");
       }
   }
   ```

### Adding a New Task Type

1. **Add to TaskType Enum**
   ```java
   public enum TaskType {
       CUSTOM_TYPE  // Add your type
   }
   ```

2. **Handle in ExecutionEngine**
   ```java
   if (task.getType() == TaskType.CUSTOM_TYPE) {
       // Handle custom task type
   }
   ```

### Adding a New Endpoint

1. **Create Request DTO** (if needed)
   ```java
   public class CustomRequest {
       @NotBlank
       private String field;
   }
   ```

2. **Add Controller Method**
   ```java
   @PostMapping("/custom")
   public ResponseEntity<ApiResponse<CustomResponse>> customEndpoint(
       @Valid @RequestBody CustomRequest request) {
       // Implementation
   }
   ```

3. **Add Service Method** (if needed)
   ```java
   public CustomResponse handleCustomRequest(CustomRequest request) {
       // Business logic
   }
   ```

## Configuration Points

### Thread Pool Configuration

**File**: `src/main/java/com/agentic/system/config/AsyncConfig.java`

```java
ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
executor.setCorePoolSize(5);      // Initial threads
executor.setMaxPoolSize(10);      // Maximum threads
executor.setQueueCapacity(50);    // Queue size
```

### Queue Configuration

**File**: `src/main/java/com/agentic/system/service/TaskQueue.java`

```java
PriorityBlockingQueue<Task> queue = 
    new PriorityBlockingQueue<>(
        100,  // Max queue size
        Comparator...  // Priority ordering
    );
```

### Retry Configuration

**File**: `src/main/java/com/agentic/system/service/ExecutionEngine.java`

```java
private static final int MAX_RETRIES = 3;
private static final long RETRY_DELAY_MS = 5000;
```

## Testing Guide

### Unit Testing

```java
@Test
public void testAgentCreation() {
    CreateAgentRequest request = new CreateAgentRequest();
    request.setName("Test Agent");
    request.setType("ANALYZER");
    
    Agent result = agentService.createAgent(request);
    
    assertNotNull(result);
    assertEquals("Test Agent", result.getName());
}
```

### Integration Testing

```java
@SpringBootTest
@AutoConfigureMockMvc
public class AgentControllerIntegrationTest {
    @Test
    public void testCreateAgentEndpoint() throws Exception {
        mockMvc.perform(post("/api/v1/agents")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
            .andExpect(status().isCreated());
    }
}
```

## Debugging Tips

### Enable Debug Logging

```properties
logging.level.com.agentic=DEBUG
```

### Check Queue Status

```bash
curl http://localhost:8080/api/v1/tasks/stats/queue
```

### Monitor Execution

```bash
curl http://localhost:8080/api/v1/executions/stats/all
```

### View Agent Status

```bash
curl http://localhost:8080/api/v1/agents
```

## Performance Considerations

### Optimization Tips

1. **Connection Pooling**: Already configured in AppConfig
2. **Caching**: Enable Spring Cache (@EnableCaching)
3. **Async Processing**: Use @Async for long operations
4. **Database Indexing**: Add indexes on frequently queried fields
5. **Batch Operations**: Process tasks in batches when possible

### Scalability Strategies

1. **Horizontal Scaling**: Deploy multiple instances
2. **Load Balancing**: Use Nginx/HAProxy
3. **Message Queue**: Integrate Kafka/RabbitMQ
4. **Database Sharding**: Partition data for large datasets
5. **Caching Layer**: Add Redis for distributed cache

## Common Issues and Solutions

### Issue: "Agent not found" error

**Solution**: Verify agent ID exists
```bash
curl http://localhost:8080/api/v1/agents
```

### Issue: Queue size growing indefinitely

**Solution**: Increase thread pool or check for failed tasks
```bash
curl http://localhost:8080/api/v1/tasks/stats/queue
```

### Issue: High memory usage

**Solution**: Reduce queue size or implement cleanup
```properties
# Reduce queue capacity in TaskQueue.java
PriorityBlockingQueue<Task> queue = 
    new PriorityBlockingQueue<>(50);  // Reduced from 100
```

## Contributing Guidelines

### Code Style
- Follow Spring Boot conventions
- Use meaningful variable names
- Add Javadoc for public methods
- Keep methods focused and small

### Git Workflow
```bash
git checkout -b feature/your-feature
git commit -m "Add your feature"
git push origin feature/your-feature
```

### Testing Requirements
- Unit test for new services
- Integration test for new endpoints
- Maintain >80% code coverage

### Documentation
- Update README.md for user-facing changes
- Update API_DOCUMENTATION.md for API changes
- Add comments for complex logic

## Version Management

Current Version: **1.0.0**

Versioning Strategy:
- MAJOR: Breaking changes
- MINOR: New features
- PATCH: Bug fixes

## Support and Resources

- **IntelliJ IDE**: Great for exploring code structure
- **Spring Boot Docs**: https://spring.io/projects/spring-boot
- **Maven Docs**: https://maven.apache.org/
- **JUnit 5 Docs**: https://junit.org/junit5/

## Next Steps for Developers

1. **Set up IDE**: Import project into IntelliJ IDEA
2. **Run the application**: `mvn spring-boot:run`
3. **Run tests**: `mvn test`
4. **Explore code**: Start with controllers and trace through services
5. **Add features**: Implement custom agent types or task types
6. **Optimize**: Profile and optimize performance bottlenecks

---

**Happy Coding! 🚀**
