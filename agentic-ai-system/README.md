# Agentic AI System - REST API

A comprehensive Spring Boot-based REST API system for autonomous agent management and task execution. This system provides a complete framework for creating, managing, and orchestrating autonomous agents to perform various types of tasks.

## Features

- **Agent Management**: Create, manage, and monitor autonomous agents
- **Task Execution**: Submit tasks to agents with priority-based queue management
- **Async Execution**: Non-blocking task execution with async processing
- **Task Retry Logic**: Automatic retry mechanism for failed tasks
- **Execution Monitoring**: Track execution results and statistics
- **Multiple Agent Types**: Support for different agent types (Analyzer, Processor, Validator, Executor, Monitor, Integrator)
- **RESTful API**: Complete REST API for all operations
- **Cross-Origin Support**: CORS enabled for frontend integration
- **Comprehensive Logging**: Detailed logging for debugging and monitoring

## Tech Stack

- **Spring Boot 3.0.5**
- **Spring Data JPA**
- **Spring Security**
- **H2 Database** (In-memory, can be replaced)
- **Lombok**
- **Maven**

## Project Structure

```
agentic-ai-system/
├── src/main/java/com/agentic/system/
│   ├── AgenticAISystemApplication.java    # Main application class
│   ├── config/                             # Configuration classes
│   │   ├── AppConfig.java
│   │   └── AsyncConfig.java
│   ├── core/                               # Core domain models
│   │   ├── Agent.java
│   │   ├── AgentType.java
│   │   ├── AgentStatus.java
│   │   ├── Task.java
│   │   ├── TaskType.java
│   │   ├── TaskStatus.java
│   │   └── ExecutionResult.java
│   ├── dto/                                # Data Transfer Objects
│   │   ├── ApiResponse.java
│   │   ├── CreateAgentRequest.java
│   │   └── CreateTaskRequest.java
│   ├── service/                            # Business logic services
│   │   ├── AgentService.java
│   │   ├── TaskService.java
│   │   ├── TaskQueue.java
│   │   └── ExecutionEngine.java
│   └── controller/                         # REST Controllers
│       ├── AgentController.java
│       ├── TaskController.java
│       └── ExecutionController.java
├── src/main/resources/
│   └── application.properties              # Application configuration
└── pom.xml                                 # Maven dependencies
```

## Getting Started

### Prerequisites

- Java 11 or higher
- Maven 3.6+

### Installation

1. Clone the repository
```bash
git clone <repository-url>
cd agentic-ai-system
```

2. Build the project
```bash
mvn clean install
```

3. Run the application
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Endpoints

### Agent Management

#### Create Agent
```http
POST /api/v1/agents
Content-Type: application/json

{
  "name": "Code Analyzer",
  "description": "Analyzes Java code for issues",
  "type": "ANALYZER",
  "capabilities": {
    "language": "java",
    "framework": "spring-boot"
  },
  "config": {
    "timeout": "30000"
  }
}
```

#### Get Agent
```http
GET /api/v1/agents/{agentId}
```

#### Get All Agents
```http
GET /api/v1/agents
```

#### Get Agents by Type
```http
GET /api/v1/agents/type/{type}
```

#### Get Agent Status
```http
GET /api/v1/agents/{agentId}/status
```

#### Update Agent Status
```http
PUT /api/v1/agents/{agentId}/status?status=RUNNING
```

#### Get Active Agents
```http
GET /api/v1/agents/status/active
```

#### Delete Agent
```http
DELETE /api/v1/agents/{agentId}
```

### Task Management

#### Create Task
```http
POST /api/v1/tasks
Content-Type: application/json

{
  "name": "Analyze Redis Caching",
  "description": "Analyze redis-caching project",
  "type": "ANALYSIS",
  "agentId": "{agentId}",
  "input": {
    "project_path": "/path/to/project"
  },
  "priority": 8,
  "maxRetries": 3
}
```

#### Get Task
```http
GET /api/v1/tasks/{taskId}
```

#### Get All Tasks
```http
GET /api/v1/tasks
```

#### Get Tasks by Agent
```http
GET /api/v1/tasks/agent/{agentId}
```

#### Get Tasks by Status
```http
GET /api/v1/tasks/status/{status}
```

#### Cancel Task
```http
PUT /api/v1/tasks/{taskId}/cancel
```

#### Get Queue Statistics
```http
GET /api/v1/tasks/stats/queue
```

### Execution Management

#### Get Execution Result
```http
GET /api/v1/executions/{taskId}
```

#### Get All Execution Results
```http
GET /api/v1/executions
```

#### Get Execution Statistics
```http
GET /api/v1/executions/stats/all
```

## Agent Types

1. **ANALYZER** - Analyzes code and identifies issues
2. **PROCESSOR** - Processes data in batches
3. **VALIDATOR** - Validates data against rules
4. **EXECUTOR** - Executes specific tasks
5. **MONITOR** - Monitors systems and sends alerts
6. **INTEGRATOR** - Orchestrates integrations
7. **CUSTOM** - Custom agent implementations

## Task Status Flow

```
PENDING -> QUEUED -> RUNNING -> COMPLETED/FAILED
                        |
                      RETRY (if maxRetries not reached)
                        |
                        v
                      FAILED (after max retries)
                        
                      CANCELLED (by user)
```

## Configuration

### Key Properties

- `server.port`: Server port (default: 8080)
- `spring.jpa.hibernate.ddl-auto`: Database DDL strategy (default: create-drop)
- `logging.level.com.agentic`: Application log level

### Thread Pool Configuration

- **Task Executor**: 10 core threads, 20 max threads, 100 queue capacity
- **Async Executor**: 5 core threads, 10 max threads, 50 queue capacity

## Usage Examples

### Example 1: Create Agent and Submit Task

```bash
# 1. Create an analyzer agent
curl -X POST http://localhost:8080/api/v1/agents \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Java Analyzer",
    "description": "Analyzes Java projects",
    "type": "ANALYZER"
  }'

# Response: {"success":true,"message":"Agent created successfully","data":{"id":"abc123",...}}

# 2. Create and submit a task
curl -X POST http://localhost:8080/api/v1/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Analyze Project",
    "type": "ANALYSIS",
    "agentId": "abc123",
    "input": {"project": "spring-boot-projects"}
  }'

# Response: {"success":true,"message":"Task created and queued successfully","data":{"id":"task123",...}}

# 3. Get task status
curl http://localhost:8080/api/v1/tasks/task123

# 4. Get execution result
curl http://localhost:8080/api/v1/executions/task123
```

### Example 2: Monitor Queue

```bash
# Get queue statistics
curl http://localhost:8080/api/v1/tasks/stats/queue

# Response: {"success":true,"data":{"queue_size":5,"pending_count":3,"completed_count":10}}
```

## API Response Format

All endpoints follow a consistent response format:

```json
{
  "success": true/false,
  "message": "Operation message",
  "data": {},
  "error": "Error message (if applicable)",
  "timestamp": 1234567890
}
```

## Error Handling

The API returns appropriate HTTP status codes:

- `200 OK` - Successful GET/PUT request
- `201 Created` - Successful POST request
- `400 Bad Request` - Invalid request data
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

## Performance Considerations

1. **Task Queue**: Priority-based queue with max 100 tasks
2. **Thread Pool**: Configurable thread pool for async execution
3. **Caching**: Simple in-memory caching enabled
4. **Database**: H2 in-memory database for development (use PostgreSQL for production)

## Security Considerations

1. **CORS**: Enabled for cross-origin requests
2. **Spring Security**: Can be enabled in configuration
3. **API Rate Limiting**: Can be added via Spring Cloud Gateway
4. **JWT**: JWT dependency included but not yet integrated

## Monitoring

Access Actuator endpoints:
- Health: `http://localhost:8080/actuator/health`
- Metrics: `http://localhost:8080/actuator/metrics`
- Info: `http://localhost:8080/actuator/info`

## Logging

Application logs are configured in `application.properties`. Key log levels:

- `com.agentic` - Application level (DEBUG)
- `org.springframework.web` - Web framework (INFO)
- `root` - All logs (INFO)

## Future Enhancements

1. **Database Persistence** - Replace H2 with PostgreSQL/MySQL
2. **WebSocket Support** - Real-time task updates
3. **Message Queue** - Integration with RabbitMQ/Kafka
4. **ML Integration** - AI model inference for agent decisions
5. **Advanced Monitoring** - Prometheus metrics and Grafana dashboards
6. **Authentication** - JWT-based authentication
7. **Rate Limiting** - API rate limiting
8. **Docker Support** - Containerization

## License

This project is licensed under the MIT License.

## Support

For issues, questions, or contributions, please contact the development team.
