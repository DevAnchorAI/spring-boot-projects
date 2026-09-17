# Quick Start Guide - Circuit Breaker Service

## 1. Build the Application

```bash
cd circuit-breaker-service
mvn clean install
```

## 2. Run the Application

```bash
# Run with default (dev) profile
mvn spring-boot:run

# Or run the JAR
java -jar target/circuit-breaker-service-1.0.0.jar
```

The application will start on `http://localhost:8080`

## 3. Test the Service

### Health Check
```bash
curl http://localhost:8080/api/health
```

### Get User (Circuit Breaker Demo)
```bash
curl http://localhost:8080/api/users/1
```

Response when service is available:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "source": "service"
  }
}
```

Response when circuit is OPEN (fallback):
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Default User",
    "email": "default@example.com",
    "source": "FALLBACK"
  }
}
```

### Process Payment (with Retry)
```bash
curl -X POST http://localhost:8080/api/payments/process \
  -H "Content-Type: application/json" \
  -d '{
    "transactionId": "TXN001",
    "amount": 99.99,
    "currency": "USD",
    "method": "CREDIT_CARD",
    "description": "Test Payment"
  }'
```

### Send Notification (with Circuit Breaker)
```bash
curl -X POST http://localhost:8080/api/notifications/send \
  -H "Content-Type: application/json" \
  -d '{
    "recipient": "user@example.com",
    "subject": "Test Notification",
    "message": "This is a test notification",
    "channel": "EMAIL"
  }'
```

## 4. Monitor Circuit Breaker State

### Check Actuator Health
```bash
curl http://localhost:8080/actuator/health
```

### Check Circuit Breaker Details
```bash
curl http://localhost:8080/actuator/circuitbreakers
```

### View Metrics
```bash
curl http://localhost:8080/actuator/metrics
```

## 5. Observe Circuit Breaker Behavior

To see the Circuit Breaker in action:

1. **Healthy State (CLOSED)**
   - Make requests to `/api/users/1`
   - All requests succeed

2. **Failure State (OPEN)**
   - Stop the external service (if it exists)
   - Make multiple requests to trigger failures
   - Watch the failure rate increase
   - Circuit will OPEN automatically
   - Requests will return fallback data immediately

3. **Recovery State (HALF_OPEN)**
   - After wait duration (5-15 seconds depending on service)
   - Circuit enters HALF_OPEN state
   - Limited requests test if service is recovered
   - If successful → Circuit closes
   - If failed → Circuit opens again

## 6. Configuration

Edit `src/main/resources/application.yml` to customize:

### Circuit Breaker Settings
- `failureRateThreshold` - When to open circuit
- `slidingWindowSize` - How many calls to evaluate
- `waitDurationInOpenState` - How long to wait before testing recovery
- `permittedNumberOfCallsInHalfOpenState` - How many test calls

### Retry Settings
- `maxAttempts` - Maximum retry attempts
- `waitDuration` - Wait time between retries

### Example: Lower Failure Threshold
```yaml
resilience4j:
  circuitbreaker:
    instances:
      userServiceBreaker:
        failureRateThreshold: 30  # Open circuit at 30% failures
```

## 7. Production Configuration

For production, use the `prod` profile:
```bash
java -jar target/circuit-breaker-service-1.0.0.jar --spring.profiles.active=prod
```

This uses more conservative thresholds to prevent false positives.

## 8. Logging

Check logs to see Circuit Breaker in action:
```
CircuitBreakerService: Calling User Service for userId: 1
CircuitBreakerService: Circuit Breaker fallback triggered for userId: 1
```

## 9. Integrate with Your Project

1. Add Resilience4j dependency to your pom.xml
2. Annotate your service methods with `@CircuitBreaker`
3. Implement fallback methods
4. Configure via application.yml
5. Monitor via Spring Boot Actuator

## Key Points to Remember

✅ Circuit Breaker prevents cascading failures
✅ Fallback methods provide graceful degradation
✅ Retry logic handles transient failures
✅ Time Limiter prevents hanging requests
✅ Metrics help you monitor service health
✅ Configuration is environment-specific

## Next Steps

- Review the comprehensive README.md for detailed documentation
- Study the ExternalServiceClient.java to understand implementation
- Run unit tests: `mvn test`
- Integrate with your microservices
- Set up monitoring with Prometheus/Grafana
