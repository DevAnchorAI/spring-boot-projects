# Circuit Breaker Architecture & Design

## System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                     Client Applications                          │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
        ┌──────────────────────────────────┐
        │   Circuit Breaker Service        │
        │   (Spring Boot Application)      │
        └──────────────┬───────────────────┘
                       │
        ┌──────────────┼──────────────────┐
        │              │                  │
        ▼              ▼                  ▼
   ┌─────────┐   ┌─────────┐        ┌─────────┐
   │  User   │   │ Payment │        │  Notif  │
   │ Service │   │ Service │        │ Service │
   │ Breaker │   │ Breaker │        │ Breaker │
   └────┬────┘   └────┬────┘        └────┬────┘
        │             │                  │
        ▼             ▼                  ▼
   ┌─────────┐   ┌─────────┐        ┌─────────┐
   │External │   │External │        │External │
   │  User   │   │ Payment │        │  Notif  │
   │ Service │   │ Service │        │ Service │
   └─────────┘   └─────────┘        └─────────┘
```

## Circuit Breaker State Machine

```
                    ┌──────────────┐
                    │    CLOSED    │
                    │              │
                    │ Requests: ✓  │
                    └──────┬───────┘
                           │
                    Failure Rate > Threshold
                    (e.g., 50%)
                           │
                           ▼
                    ┌──────────────┐
         ┌─────────▶│     OPEN     │◀─────────┐
         │          │              │          │
         │          │ Requests: ✗  │          │
         │          │ (Fallback)   │          │
         │          └──────┬───────┘          │
         │                 │                  │
         │         Wait Duration              │
         │         Elapsed (5-15s)            │
         │                 │                  │
         │                 ▼                  │
         │          ┌──────────────┐          │
         └──────────│  HALF_OPEN   │          │
                    │              │          │
                    │ Requests: ◐  │          │
                    │ (Limited)    │          │
                    └──────┬───────┘          │
                           │                  │
            Success (3/3) ──┘                 │
                           │                  │
                    Failure (>0) ─────────────┘
```

## Component Interaction

### 1. CircuitBreakerController
- REST endpoints for clients
- Handles HTTP requests/responses
- Delegates to service layer

### 2. ExternalServiceClient
- Contains resilience patterns
- Manages circuit breaker state
- Implements fallback logic
- Uses RestTemplate for HTTP calls

### 3. Resilience4j Patterns
- **Circuit Breaker**: State machine for failure handling
- **Retry**: Automatic retry with backoff
- **Time Limiter**: Timeout enforcement
- **Bulkhead**: Thread pool isolation

## Detailed Flow Diagrams

### Successful Request Flow (CLOSED State)

```
Client Request
     │
     ▼
┌─────────────────────────┐
│ CircuitBreakerController │
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│ ExternalServiceClient   │
│ (Circuit: CLOSED)       │
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│ Call External Service   │
│ (HTTP Request)          │
└────────┬────────────────┘
         │
         ▼ (Success)
┌─────────────────────────┐
│ Record Success          │
│ Increment counters      │
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│ Return Response         │
│ to Client               │
└─────────────────────────┘
```

### Failed Request Flow (OPEN State)

```
Client Request
     │
     ▼
┌─────────────────────────┐
│ CircuitBreakerController │
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│ ExternalServiceClient   │
│ (Circuit: OPEN)         │
└────────┬────────────────┘
         │
   Circuit is OPEN
   (Failure rate exceeded)
         │
         ▼
┌─────────────────────────┐
│ Skip Service Call       │
│ (Prevent cascading fail)│
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│ Invoke Fallback Method  │
│ (Graceful Degradation)  │
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│ Return Fallback Response│
│ (Default/Cached Data)   │
└─────────────────────────┘
```

### Retry Flow (Transient Failures)

```
Client Request
     │
     ▼
┌─────────────────────────┐
│ Call External Service   │
│ (Attempt 1)             │
└────────┬────────────────┘
         │
    Connection Failed (Transient)
         │
         ▼
┌─────────────────────────┐
│ Wait 2000ms             │
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│ Retry (Attempt 2)       │
└────────┬────────────────┘
         │
    Retry Until:
    - Success, OR
    - Max Attempts Reached
```

## Configuration Strategy

### Default Configuration
```yaml
slidingWindowSize: 10           # Evaluate last 10 calls
minimumNumberOfCalls: 5         # Need 5 calls minimum
failureRateThreshold: 50        # 50% failures = OPEN
waitDurationInOpenState: 5s     # Wait 5s before HALF_OPEN
permittedNumberOfCallsInHalfOpenState: 3  # Test 3 calls
```

### User Service (Longer Operations)
```yaml
slowCallDurationThreshold: 2s   # > 2s = slow
waitDurationInOpenState: 10s    # Longer wait for recovery
failureRateThreshold: 60        # More lenient
```

### Payment Service (Critical)
```yaml
failureRateThreshold: 50        # Standard threshold
maxAttempts: 4                  # More retries
waitDuration: 2000ms            # Longer wait between retries
```

### Notification Service (Best Effort)
```yaml
failureRateThreshold: 40        # Lower threshold (fail fast)
waitDurationInOpenState: 5s     # Quick recovery attempt
```

## Metrics and Monitoring

### Key Metrics Collected

```
resilience4j.circuitbreaker.calls
├── Total calls
├── Successful calls
├── Failed calls
└── Slow calls

resilience4j.circuitbreaker.state
├── CLOSED (0)
├── OPEN (1)
└── HALF_OPEN (2)

resilience4j.circuitbreaker.buffered.calls
├── Current buffer size
└── Max buffer size

resilience4j.retry.calls
├── Total retries
├── Successful retries
└── Failed retries
```

### Health Indicators
```
Circuit Breaker Health
├── User Service: UP/DOWN
├── Payment Service: UP/DOWN
└── Notification Service: UP/DOWN
```

## Error Handling Strategy

### By Exception Type

1. **Transient Errors** (RetryException)
   - ConnectException
   - IOException
   - TimeoutException
   → Retry with backoff

2. **Permanent Errors** (No Retry)
   - NullPointerException
   - IllegalArgumentException
   → Fail fast with fallback

3. **Circuit Open**
   - Any request type
   → Invoke fallback immediately

## Fallback Strategy

### User Service Fallback
```java
UserResponse.builder()
    .id(userId)
    .name("Default User")
    .email("default@example.com")
    .source("FALLBACK")
    .build()
```
✓ Provides valid response structure
✓ Identifies fallback data
✓ Allows UI to degrade gracefully

### Payment Service Fallback
```java
PaymentResponse.builder()
    .transactionId("PENDING-" + timestamp)
    .status("PENDING")
    .message("Payment queued for processing")
    .build()
```
✓ Prevents data loss
✓ Queues for later processing
✓ Tracks transaction ID

### Notification Service Fallback
```java
NotificationResponse.builder()
    .notificationId("FALLBACK-" + timestamp)
    .status("QUEUED")
    .message("Queued due to service unavailability")
    .build()
```
✓ Queues notification
✓ Prevents customer notification loss
✓ Allows async processing

## Performance Considerations

### Thread Pools
```yaml
thread-pool-bulkhead:
  coreThreadPoolSize: 5
  maxThreadPoolSize: 10
  queueCapacity: 100
```
- Limits concurrent calls
- Prevents resource exhaustion
- Isolates thread pools per service

### Time Limits
```yaml
timeoutDuration: 2s
slowCallDurationThreshold: 2s
```
- Prevents hanging connections
- Forces rapid failure detection
- Enables quick circuit opening

### Retry Strategy
```yaml
maxAttempts: 3
waitDuration: 1000ms
```
- Handles transient failures
- Doesn't retry permanent failures
- Prevents retry storms

## Testing Scenarios

### Scenario 1: Normal Operation
- Circuit CLOSED
- All requests succeed
- Metrics: 100% success rate

### Scenario 2: Service Degradation
- Multiple failures
- Failure rate exceeds threshold
- Circuit opens
- Fallback invoked
- Metrics: High failure rate

### Scenario 3: Service Recovery
- Service comes back online
- Circuit transitions to HALF_OPEN
- Test requests succeed
- Circuit closes
- Metrics: Recovery in progress

### Scenario 4: Transient Failure
- Single connection error
- Automatic retry succeeds
- No circuit opening
- Metrics: One failure, then success

## Integration Patterns

### Pattern 1: Microservice Communication
```
Service A → [Circuit Breaker] → Service B
```
Prevents Service A from failing when Service B is down

### Pattern 2: External API Calls
```
Application → [Circuit Breaker] → Third-party API
```
Handles API rate limits and outages gracefully

### Pattern 3: Database Fallback
```
Service → [Circuit Breaker] → DB
                 ├─ Cache
                 └─ Read Replica
```
Routes around failing primary database

## Best Practices Applied

✅ **Separate circuit breakers** per external service
✅ **Meaningful fallbacks** - not just null/exception
✅ **Configurable** - different settings per environment
✅ **Observable** - metrics and logs for monitoring
✅ **Tested** - unit tests for all scenarios
✅ **Documented** - clear configuration and behavior
✅ **Graceful degradation** - service continues with reduced functionality
✅ **Retry logic** - handles transient failures
✅ **Timeout protection** - prevents indefinite waiting
✅ **Resource isolation** - bulkhead pattern for thread pools

## Comparison: Without vs With Circuit Breaker

### WITHOUT Circuit Breaker
```
Failure in Service B
    │
    ▼
Service A makes 100 requests (all fail)
    │
    ▼
Resources exhausted
    │
    ▼
Service A becomes slow
    │
    ▼
Cascading failure to Service C
```

### WITH Circuit Breaker
```
Failure in Service B
    │
    ▼
First few requests fail
    │
    ▼
Circuit opens (detects pattern)
    │
    ▼
Remaining requests return fallback immediately
    │
    ▼
Service A continues (degraded)
    │
    ▼
Resources preserved
    │
    ▼
No cascading failure
```

## Conclusion

This Circuit Breaker implementation provides:
- Resilience against cascading failures
- Graceful degradation
- Automatic service recovery
- Comprehensive monitoring
- Production-ready configuration
- Extensible architecture

Perfect for distributed microservices systems!
