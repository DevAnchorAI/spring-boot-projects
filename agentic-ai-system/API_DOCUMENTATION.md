# Agentic AI System - API Documentation

## Overview

The Agentic AI System is a comprehensive REST API framework built with Spring Boot for managing autonomous agents and executing distributed tasks.

## System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    REST API Layer                           │
│  (AgentController, TaskController, ExecutionController)     │
└──────────────┬──────────────────────────────────────────────┘
               │
┌──────────────▼──────────────────────────────────────────────┐
│                  Service Layer                              │
│  (AgentService, TaskService, ExecutionEngine, TaskQueue)    │
└──────────────┬──────────────────────────────────────────────┘
               │
┌──────────────▼──────────────────────────────────────────────┐
│                  Domain Model Layer                         │
│  (Agent, Task, ExecutionResult, AgentType, TaskStatus)      │
└─────────────────────────────────────────────────────────────┘
```

## Key Components

### 1. Agent Service
Manages agent lifecycle:
- Create new agents
- Update agent status
- Query agents by type or status
- Delete agents
- Initialize default agents

### 2. Task Service
Handles task management:
- Create and submit tasks
- Query tasks by agent or status
- Cancel tasks
- Get queue statistics

### 3. Task Queue
Priority-based queue system:
- FIFO with priority ordering
- Configurable queue size
- Task status tracking
- Queue statistics

### 4. Execution Engine
Executes tasks asynchronously:
- Picks tasks from queue
- Executes based on agent type
- Handles failures and retries
- Stores execution results
- Provides execution statistics

## API Response Structure

All API responses follow this standardized format:

```json
{
  "success": true/false,
  "message": "Descriptive message",
  "data": {},
  "error": "Error description (if applicable)",
  "timestamp": 1234567890
}
```

## Agent Types and Capabilities

### ANALYZER
- **Purpose**: Code analysis and inspection
- **Capabilities**: Static analysis, issue detection
- **Example**: Null pointer checks, performance issues

### PROCESSOR
- **Purpose**: Data processing and transformation
- **Capabilities**: Batch processing, format conversion
- **Example**: JSON transformation, data aggregation

### VALIDATOR
- **Purpose**: Data validation and verification
- **Capabilities**: Schema validation, business rule checking
- **Example**: Input validation, data consistency checks

### EXECUTOR
- **Purpose**: General task execution
- **Capabilities**: Command execution, workflow automation
- **Example**: Build jobs, deployment tasks

### MONITOR
- **Purpose**: System monitoring and alerting
- **Capabilities**: Health checks, metric collection
- **Example**: Performance monitoring, log analysis

### INTEGRATOR
- **Purpose**: System integration and orchestration
- **Capabilities**: API calls, system coordination
- **Example**: Service-to-service communication

### CUSTOM
- **Purpose**: User-defined agent types
- **Capabilities**: Extensible with custom logic

## Task Lifecycle

```
┌─────────┐
│ PENDING │  Task created but not yet queued
└────┬────┘
     │
     ▼
┌─────────┐
│ QUEUED  │  Task waiting in priority queue
└────┬────┘
     │
     ▼
┌─────────┐      ┌────────┐
│ RUNNING │─────▶│ RETRY  │  Task failed, retry scheduled
└────┬────┘      └────┬───┘
     │                │
     ├────────────────┘
     │
     ▼
┌───────────────────────────────────┐
│ COMPLETED or FAILED or CANCELLED  │
└───────────────────────────────────┘
```

## Example Use Cases

### Use Case 1: Code Analysis Workflow
```
1. Create ANALYZER agent
   ↓
2. Submit ANALYSIS task with project path
   ↓
3. Task queued and executed by analyzer
   ↓
4. Results stored with detected issues
   ↓
5. Retrieve results via execution endpoint
```

### Use Case 2: Data Processing Pipeline
```
1. Create PROCESSOR agent
   ↓
2. Submit PROCESSING task with data
   ↓
3. Agent processes in batches
   ↓
4. Results aggregated and stored
   ↓
5. Monitor progress via queue stats
```

### Use Case 3: Multi-Stage Validation
```
1. Create VALIDATOR agent
   ↓
2. Submit multiple VALIDATION tasks
   ↓
3. Tasks queued and executed in priority order
   ↓
4. Failed tasks auto-retry
   ↓
5. Collect all results
```

## Performance Characteristics

| Metric | Value |
|--------|-------|
| Max Queue Size | 100 tasks |
| Max Concurrent Tasks | 10 |
| Thread Pool Core Size | 10 |
| Thread Pool Max Size | 20 |
| Default Retry Count | 3 |
| Max Retry Delay | 5 seconds |

## Error Handling Strategy

1. **Validation Errors**: Immediate 400 response with validation details
2. **Resource Not Found**: 404 response
3. **Business Logic Errors**: 400 response with error description
4. **Server Errors**: 500 response with error trace
5. **Async Task Failures**: 
   - Automatic retry (up to maxRetries)
   - Final failure after retries exhausted
   - Results logged and stored

## Security Considerations

### Current Features
- CORS enabled
- Request validation via @Valid annotations
- Input sanitization

### Recommended Future Enhancements
- JWT authentication
- Role-based access control (RBAC)
- API rate limiting
- Request signing
- Audit logging

## Monitoring and Observability

### Built-in Endpoints
- `/actuator/health` - Application health
- `/actuator/metrics` - Application metrics
- `/actuator/info` - Application information

### Application Logs
- `DEBUG` - com.agentic.* (application logic)
- `INFO` - org.springframework.web (HTTP requests)
- `INFO` - root (general framework)

### Key Metrics
- Queue size
- Pending tasks count
- Completed/Failed task counts
- Execution success rate
- Agent status distribution

## Integration Patterns

### Pattern 1: Fire and Forget
```
POST /api/v1/tasks -> Get taskId -> Monitor via GET /api/v1/tasks/{taskId}
```

### Pattern 2: Polling
```
Create task -> Poll /api/v1/executions/{taskId} until completion
```

### Pattern 3: Batch Processing
```
Create multiple tasks -> Get queue stats -> Wait for completion
```

### Pattern 4: Agent Delegation
```
Get active agents -> Select appropriate agent -> Submit task to that agent
```

## Database Schema

### Agents Table
```
- id (UUID)
- name (String)
- description (String)
- type (AgentType)
- status (AgentStatus)
- capabilities (JSON)
- config (JSON)
- createdAt (DateTime)
- updatedAt (DateTime)
- createdBy (String)
```

### Tasks Table
```
- id (UUID)
- name (String)
- description (String)
- type (TaskType)
- status (TaskStatus)
- agentId (FK to Agents)
- input (JSON)
- output (JSON)
- createdAt (DateTime)
- startedAt (DateTime)
- completedAt (DateTime)
- priority (Integer)
- retryCount (Integer)
- maxRetries (Integer)
- error (String)
- executionTimeMs (Long)
```

### ExecutionResults Table
```
- taskId (FK to Tasks)
- agentId (FK to Agents)
- success (Boolean)
- result (JSON)
- message (String)
- error (String)
- executedAt (DateTime)
- executionTimeMs (Long)
- metadata (JSON)
```

## Deployment Considerations

### Horizontal Scaling
- Stateless service design
- Distributed task queue with message broker
- Shared database for persistence
- Load balancer for API endpoints

### High Availability
- Multiple service instances
- Database replication
- Message queue failover
- Health checks and auto-restart

### Production Checklist
- [ ] Switch from H2 to PostgreSQL/MySQL
- [ ] Enable authentication (JWT)
- [ ] Configure SSL/TLS
- [ ] Set up monitoring and alerts
- [ ] Configure log aggregation
- [ ] Implement rate limiting
- [ ] Set up backup and recovery
- [ ] Configure CORS properly
- [ ] Load test the system

## Troubleshooting

### Common Issues

**Issue**: Queue size keeps growing
**Solution**: Increase thread pool size or add more executor instances

**Issue**: Tasks timing out
**Solution**: Increase task timeout or implement timeout handling in agent

**Issue**: High memory usage
**Solution**: Reduce queue size or implement task result cleanup

**Issue**: Database connection issues
**Solution**: Check connection pool settings and database availability

## Future Roadmap

1. **Phase 1** (Current)
   - Basic agent and task management
   - Priority-based task queue
   - Async execution

2. **Phase 2**
   - Database persistence
   - WebSocket real-time updates
   - Message queue integration

3. **Phase 3**
   - Machine learning integration
   - Advanced scheduling
   - Complex workflow support

4. **Phase 4**
   - Distributed tracing
   - Advanced analytics
   - Custom agent framework

## Support and Documentation

- API Documentation: See README.md
- Quick Start: See QUICKSTART.md
- Integration Guide: See individual agent documentation
- Examples: See example scripts

## Version History

- **v1.0.0** (Current)
  - Initial release
  - Agent management
  - Task execution framework
  - RESTful API
