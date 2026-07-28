# Agentic AI System - REST API

A Spring Boot REST API for autonomous agent management and task execution.

## Quick Start (5 minutes)

```bash
# Build
cd agentic-ai-system
mvn clean install

# Run
mvn spring-boot:run

# Test (PowerShell)
.\test-api.ps1
```

API runs on: `http://localhost:8080/api/v1`

## API Endpoints

### Agents
- `POST /agents` - Create agent
- `GET /agents` - Get all agents
- `GET /agents/{id}` - Get agent
- `DELETE /agents/{id}` - Delete agent

### Tasks
- `POST /tasks` - Create task
- `GET /tasks` - Get all tasks
- `GET /tasks/{id}` - Get task
- `PUT /tasks/{id}/cancel` - Cancel task

### Execution
- `GET /executions/{taskId}` - Get result
- `GET /executions/stats/all` - Get statistics

## Project Structure

```
src/main/java/com/agentic/system/
├── AgenticAISystemApplication.java
├── config/              (Configuration)
├── core/                (Domain models)
├── dto/                 (Data Transfer Objects)
├── service/             (Business logic)
└── controller/          (REST endpoints)
```

## Technology

- Spring Boot 3.0.5
- Java 11+
- Maven
- H2 Database
- Lombok

## Create an Agent

```bash
curl -X POST http://localhost:8080/api/v1/agents \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Analyzer",
    "type": "ANALYZER"
  }'
```

## Submit a Task

```bash
curl -X POST http://localhost:8080/api/v1/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Analyze",
    "type": "ANALYSIS",
    "agentId": "YOUR_AGENT_ID"
  }'
```

## Configuration

Edit `application.properties`:
- `server.port` - Server port (default: 8080)
- `logging.level.com.agentic` - Log level (default: DEBUG)

## For Production

See `DEPLOYMENT_GUIDE.md` for:
- Docker deployment
- Kubernetes setup
- Database configuration
- Security setup

## Features

✅ Agent management
✅ Priority-based task queue
✅ Asynchronous execution
✅ Automatic retry (3 retries)
✅ Execution monitoring
✅ RESTful API
✅ Error handling
✅ Logging
