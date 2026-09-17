# Circuit Breaker Service - Complete Guide

## Overview

This is a comprehensive Spring Boot microservice that demonstrates the **Circuit Breaker Pattern** using **Resilience4j**. The Circuit Breaker pattern is a critical resilience pattern used to prevent cascading failures in distributed systems.

## What is the Circuit Breaker Pattern?

The Circuit Breaker pattern acts like an electrical circuit breaker in your home - when there's a fault (excessive failures), it "trips" to prevent further damage:

### Three States:

1. **CLOSED** - Normal operation, all requests pass through
2. **OPEN** - Circuit is broken, requests fail immediately with fallback
3. **HALF_OPEN** - Testing if service recovered, limited requests allowed

### State Transitions:

```
CLOSED → (failure threshold reached) → OPEN → (wait time elapsed) → HALF_OPEN → (success) → CLOSED
                                                              ↓ (failure)
                                                            OPEN
```

## Project Structure

```
circuit-breaker-service/
├── pom.xml                                    # Maven configuration
├── src/
│   ├── main/
│   │   ├── java/com/example/circuitbreaker/
│   │   │   ├── CircuitBreakerApplication.java # Main Spring Boot app
│   │   │   ├── controller/
│   │   │   │   └── CircuitBreakerController.java # REST endpoints
│   │   │   └── service/
│   │   │       ├── ExternalServiceClient.java  # Circuit Breaker logic
│   │   │       ├── UserResponse.java            # User DTO
│   │   │       ├── PaymentDtos.java             # Payment DTOs
│   │   │       └── NotificationDtos.java        # Notification DTOs
│   │   └── resources/
│   │       └── application.yml                  # Configuration
│   └── test/
│       └── java/com/example/circuitbreaker/
│           └── service/CircuitBreakerServiceTests.java
└── README.md
```

## Key Features

### 1. **Circuit Breaker Pattern Implementation**
- Three service clients with separate circuit breakers
- User Service Circuit Breaker
- Payment Service Circuit Breaker
- Notification Service Circuit Breaker

### 2. **Fallback Mechanisms**
- Graceful degradation when services fail
- Default responses returned to clients
- User data returns default user with "FALLBACK" source
- Payment returns "PENDING" status
- Notification returns "QUEUED" status

### 3. **Retry Logic**
- Automatic retry on transient failures
- Configurable retry attempts and wait times
- Different retry policies per service

### 4. **Time Limiter**
- Prevents hanging requests
- Configurable timeout duration
- Works with async/await patterns

### 5. **Metrics & Monitoring**
- Spring Boot Actuator integration
- Prometheus-ready metrics
- Circuit breaker health indicators
- Real-time state monitoring

## Configuration Details

### Application Configuration (application.yml)

```yaml
resilience4j:
  circuitbreaker:
    configs:
      default:
        slidingWindowSize: 10              # Last 10 calls evaluated
        minimumNumberOfCalls: 5            # Minimum calls before state change
        permittedNumberOfCallsInHalfOpenState: 3  # Test calls in half-open
        failureRateThreshold: 50           # 50% failure = open circuit
        waitDurationInOpenState: 5s        # Wait 5s before half-open
        slowCallRateThreshold: 50          # Consider slow calls as failures
        slowCallDurationThreshold: 2s      # Calls > 2s are slow
```

### Per-Service Configuration

- **User Service**: Higher timeout (3s), 60% failure threshold
- **Payment Service**: Medium timeout (2s), 50% failure threshold, more retries
- **Notification Service**: Lower failure threshold (40%), shorter open time

## Resilience4j Features Used

### 1. Circuit Breaker
```java
@CircuitBreaker(
    name = "userServiceBreaker",
    fallbackMethod = "userServiceFallback"
)
```

### 2. Retry
```java
@Retry(name = "userServiceRetry")
```

### 3. Time Limiter
```java
@TimeLimiter(name = "userServiceTimeLimiter")
```

### 4. Bulkhead (Thread Pool Isolation)
Configured in application.yml to limit concurrent calls

## REST API Endpoints

### 1. Get User (with Circuit Breaker)
```
GET /api/users/{userId}

Response (Success):
{
  "success": true,
  "data": {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "source": "service"
  }
}

Response (Fallback):
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

### 2. Process Payment (with Circuit Breaker)
```
POST /api/payments/process
Content-Type: application/json

Request:
{
  "transactionId": "TXN123",
  "amount": 100.00,
  "currency": "USD",
  "method": "CREDIT_CARD",
  "description": "Payment for Order #123"
}

Response (Success):
{
  "success": true,
  "data": {
    "transactionId": "TXN123",
    "status": "SUCCESS",
    "message": "Payment processed successfully"
  }
}

Response (Fallback):
{
  "success": true,
  "data": {
    "transactionId": "PENDING-1234567890",
    "status": "PENDING",
    "message": "Payment is queued. Circuit Breaker is active."
  }
}
```

### 3. Send Notification (with Circuit Breaker)
```
POST /api/notifications/send
Content-Type: application/json

Request:
{
  "recipient": "user@example.com",
  "subject": "Order Confirmation",
  "message": "Your order has been confirmed",
  "channel": "EMAIL"
}

Response (Success):
{
  "success": true,
  "data": {
    "notificationId": "NOTIF123",
    "status": "SENT",
    "message": "Notification sent successfully"
  }
}

Response (Fallback):
{
  "success": true,
  "data": {
    "notificationId": "FALLBACK-1234567890",
    "status": "QUEUED",
    "message": "Notification queued due to service unavailability"
  }
}
```

### 4. Health Check
```
GET /api/health

Response:
{
  "status": "UP",
  "service": "Circuit Breaker Service"
}
```

### 5. Actuator Endpoints
```
GET /actuator                           # List all actuator endpoints
GET /actuator/health                    # Service health
GET /actuator/info                      # Service info
GET /actuator/metrics                   # Available metrics
GET /actuator/circuitbreakers           # Circuit breaker status
GET /actuator/retries                   # Retry status
```

## How to Build and Run

### Prerequisites
- Java 17+
- Maven 3.6+

### Build
```bash
mvn clean package
```

### Run
```bash
# Development profile
java -jar target/circuit-breaker-service-1.0.0.jar

# Production profile
java -jar target/circuit-breaker-service-1.0.0.jar --spring.profiles.active=prod
```

### Access the Application
- Service: http://localhost:8080
- Health: http://localhost:8080/api/health
- Actuator: http://localhost:8080/actuator

## Testing Circuit Breaker Behavior

### Scenario 1: Healthy Service (Circuit CLOSED)
```bash
# All requests succeed
curl http://localhost:8080/api/users/1
curl http://localhost:8080/api/users/2
curl http://localhost:8080/api/users/3
```

### Scenario 2: Service Degradation (Circuit OPEN)
When failure rate exceeds threshold:
1. First 5+ requests fail
2. Circuit opens automatically
3. Next requests return fallback data immediately
4. No more calls to failing service

### Scenario 3: Service Recovery (Circuit HALF_OPEN)
After wait duration:
1. Limited requests (3) test service
2. If successful → Circuit closes
3. If failed → Circuit remains open

### Scenario 4: Retry with Backoff
```java
@Retry(name = "paymentServiceRetry")  // Retries up to 4 times
```
On transient failures (ConnectException, IOException):
- Attempt 1: Immediate
- Attempt 2: Wait 2s
- Attempt 3: Wait 2s
- Attempt 4: Wait 2s

## Monitoring and Metrics

### Spring Boot Actuator
Access metrics via:
```
http://localhost:8080/actuator/metrics
```

### Circuit Breaker Metrics
- `resilience4j.circuitbreaker.calls` - Total calls
- `resilience4j.circuitbreaker.calls.success` - Successful calls
- `resilience4j.circuitbreaker.calls.failure` - Failed calls
- `resilience4j.circuitbreaker.state` - Current state (0=CLOSED, 1=OPEN, 2=HALF_OPEN)
- `resilience4j.circuitbreaker.buffered.calls` - Buffered call count

### Example: Get Circuit Breaker Metrics
```bash
curl http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.calls?tag=name:userServiceBreaker
```

## Best Practices

### 1. **Choose Appropriate Thresholds**
```yaml
failureRateThreshold: 50        # Not too low (false positives)
slidingWindowSize: 10           # Enough samples to be meaningful
minimumNumberOfCalls: 5         # Don't open on single failure
```

### 2. **Implement Meaningful Fallbacks**
- Return cached data when possible
- Queue operations for later processing
- Provide default/degraded responses
- Log fallback invocations

### 3. **Use Appropriate Retry Strategies**
- Don't retry non-idempotent operations
- Use exponential backoff for repeated failures
- Set maximum retry attempts

### 4. **Set Reasonable Timeouts**
```yaml
timeoutDuration: 2s             # Not too short (false failures)
slowCallDurationThreshold: 2s   # Classify slow responses
```

### 5. **Monitor Circuit Breaker State**
```bash
# Check state via actuator
curl http://localhost:8080/actuator/health
```

### 6. **Test All Failure Scenarios**
- Successful calls (CLOSED)
- Excessive failures (OPEN)
- Service recovery (HALF_OPEN)
- Timeout scenarios
- Retry logic

## Common Issues and Solutions

### Issue 1: Circuit Opens Too Frequently
**Solution**: Increase `failureRateThreshold` or `minimumNumberOfCalls`

### Issue 2: Circuit Never Recovers
**Solution**: Reduce `waitDurationInOpenState` or fix underlying service

### Issue 3: Fallback Not Called
**Solution**: Ensure fallback method signature matches (including exception parameter)

### Issue 4: High Latency in Half-Open State
**Solution**: Increase `permittedNumberOfCallsInHalfOpenState` or reduce `waitDurationInOpenState`

## Integration with Other Services

To integrate this service with other microservices:

1. **Configure external service URLs** in application.yml
2. **Add service-specific circuit breakers** if needed
3. **Implement appropriate fallbacks** for your business logic
4. **Monitor circuit breaker metrics** via actuator
5. **Test failure scenarios** in staging environment

## Dependencies

- Spring Boot 3.1.5
- Resilience4j 2.1.0
- Spring Data REST
- Micrometer (for metrics)
- Lombok (for boilerplate reduction)

## Summary

This Circuit Breaker Service demonstrates:
- ✅ Resilience4j integration with Spring Boot
- ✅ Graceful degradation under failure
- ✅ Automatic service recovery
- ✅ Configurable resilience patterns
- ✅ Comprehensive monitoring and metrics
- ✅ Production-ready implementation

Use this as a template for adding circuit breaker patterns to your microservices!
