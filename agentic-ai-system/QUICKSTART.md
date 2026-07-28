# Quick Start Guide

## Running the Application

### Using Maven
```bash
mvn clean install
mvn spring-boot:run
```

### Using Java
```bash
mvn clean package
java -jar target/agentic-ai-system-1.0.0.jar
```

## First Steps

### 1. Create an Agent

```bash
curl -X POST http://localhost:8080/api/v1/agents \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Default Analyzer",
    "description": "Analyzes spring boot projects",
    "type": "ANALYZER",
    "capabilities": {
      "language": "java",
      "framework": "spring-boot"
    },
    "config": {
      "timeout": "30000"
    }
  }'
```

**Note**: Default agents are automatically created on startup.

### 2. Get All Agents

```bash
curl http://localhost:8080/api/v1/agents
```

### 3. Create a Task

```bash
curl -X POST http://localhost:8080/api/v1/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Analyze Redis Project",
    "description": "Analyze redis-caching project for issues",
    "type": "ANALYSIS",
    "agentId": "<AGENT_ID_FROM_STEP_2>",
    "input": {
      "project_path": "redis-caching"
    },
    "priority": 8,
    "maxRetries": 3
  }'
```

### 4. Monitor Task Execution

```bash
# Get task status
curl http://localhost:8080/api/v1/tasks/<TASK_ID>

# Get task by agent
curl http://localhost:8080/api/v1/tasks/agent/<AGENT_ID>

# Get queue statistics
curl http://localhost:8080/api/v1/tasks/stats/queue
```

### 5. Get Execution Results

```bash
# Get result for specific task
curl http://localhost:8080/api/v1/executions/<TASK_ID>

# Get all results
curl http://localhost:8080/api/v1/executions

# Get execution statistics
curl http://localhost:8080/api/v1/executions/stats/all
```

## Default Agents

Three default agents are created automatically:

1. **Default Analyzer**
   - Type: ANALYZER
   - Capabilities: Java, Spring Boot analysis
   - Purpose: Code analysis and issue detection

2. **Default Processor**
   - Type: PROCESSOR
   - Capabilities: JSON format, batch processing
   - Purpose: Data processing and transformation

3. **Default Validator**
   - Type: VALIDATOR
   - Capabilities: Standard validation rules
   - Purpose: Data validation and verification

## Key Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/v1/agents | Create agent |
| GET | /api/v1/agents | Get all agents |
| GET | /api/v1/agents/{id} | Get agent |
| GET | /api/v1/agents/type/{type} | Get agents by type |
| DELETE | /api/v1/agents/{id} | Delete agent |
| POST | /api/v1/tasks | Create task |
| GET | /api/v1/tasks | Get all tasks |
| GET | /api/v1/tasks/{id} | Get task |
| GET | /api/v1/tasks/agent/{agentId} | Get tasks by agent |
| GET | /api/v1/tasks/status/{status} | Get tasks by status |
| PUT | /api/v1/tasks/{id}/cancel | Cancel task |
| GET | /api/v1/executions/{taskId} | Get execution result |
| GET | /api/v1/executions/stats/all | Get execution stats |

## Example Workflow

```
1. Application starts
   ↓
2. 3 default agents created (Analyzer, Processor, Validator)
   ↓
3. Create additional agents as needed
   ↓
4. Submit tasks to agents
   ↓
5. Tasks queued in priority-based queue
   ↓
6. Execution engine picks tasks from queue
   ↓
7. Agents execute tasks asynchronously
   ↓
8. Results stored and can be retrieved
   ↓
9. Monitor queue statistics and execution stats
```

## Configuration

### Change Server Port
Edit `application.properties`:
```properties
server.port=9090
```

### Enable Debug Logging
Edit `application.properties`:
```properties
logging.level.com.agentic=DEBUG
```

### Change Database
Edit `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/agentic_db
spring.datasource.username=root
spring.datasource.password=password
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
```

## API Response Examples

### Successful Response
```json
{
  "success": true,
  "message": "Agent created successfully",
  "data": {
    "id": "12345",
    "name": "Java Analyzer",
    "type": "ANALYZER",
    "status": "IDLE",
    "createdAt": "2024-01-15T10:30:00"
  },
  "timestamp": 1705318200000
}
```

### Error Response
```json
{
  "success": false,
  "message": "Failed to create agent",
  "error": "Agent type is required",
  "timestamp": 1705318200000
}
```

## Troubleshooting

### Port Already in Use
```bash
# On Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# On Linux/Mac
lsof -i :8080
kill -9 <PID>
```

### Dependency Issues
```bash
mvn clean install -DskipTests
```

### Database Issues
The application uses H2 in-memory database by default. If you need to reset:
1. Stop the application
2. Start again (H2 will be recreated)

## Next Steps

1. Integrate with your frontend application
2. Add more custom agent implementations
3. Configure database persistence for production
4. Implement authentication and authorization
5. Set up monitoring and logging infrastructure
6. Deploy to cloud platform (AWS, GCP, Azure)

## Support

For issues or questions:
1. Check application logs: `tail -f logs/application.log`
2. Review API documentation in README.md
3. Check endpoint health: `http://localhost:8080/actuator/health`
