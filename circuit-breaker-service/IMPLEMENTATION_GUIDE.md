# Implementation Guide - Circuit Breaker with Resilience4j

## Table of Contents
1. [Overview](#overview)
2. [Core Components](#core-components)
3. [Implementation Steps](#implementation-steps)
4. [Configuration](#configuration)
5. [Testing](#testing)
6. [Monitoring](#monitoring)
7. [Troubleshooting](#troubleshooting)

## Overview

This guide explains how to implement the Circuit Breaker pattern in a Spring Boot application using Resilience4j.

### Why Circuit Breaker?

**Problem:**
```
Service A → Service B → Service C
                ↓
           Service B fails
                ↓
           All requests from A timeout
                ↓
           A's resources exhausted
                ↓
           Cascading failure to other services
```

**Solution with Circuit Breaker:**
```
Service A → [Circuit Breaker] → Service B
                      ↓
                  Detects failures
                      ↓
                  Opens circuit
                      ↓
                  Returns fallback
                      ↓
                  Prevents cascading failures
```

## Core Components

### 1. Maven Dependencies

```xml
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>
    <version>2.1.0</version>
</dependency>

<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-circuitbreaker</artifactId>
    <version>2.1.0</version>
</dependency>

<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-micrometer</artifactId>
    <version>2.1.0</version>
</dependency>
```

### 2. Service Class with Circuit Breaker

```java
@Service
@Slf4j
public class ExternalServiceClient {
    
    @CircuitBreaker(
        name = "userServiceBreaker",
        fallbackMethod = "userServiceFallback"
    )
    @Retry(name = "userServiceRetry")
    @TimeLimiter(name = "userServiceTimeLimiter")
    public CompletableFuture<UserResponse> getUserById(Long userId) {
        log.info("Calling User Service for userId: {}", userId);
        // Implementation
    }
    
    // Fallback method with same signature + Exception parameter
    public CompletableFuture<UserResponse> userServiceFallback(
        Long userId, 
        Exception ex) {
        log.warn("Fallback triggered: {}", ex.getMessage());
        return CompletableFuture.completedFuture(
            UserResponse.builder()
                .id(userId)
                .name("Default User")
                .email("default@example.com")
                .source("FALLBACK")
                .build()
        );
    }
}
```

### 3. Controller Layer

```java
@RestController
@RequestMapping("/api")
public class UserController {
    
    @Autowired
    private ExternalServiceClient client;
    
    @GetMapping("/users/{userId}")
    public CompletableFuture<ResponseEntity<UserResponse>> getUser(
        @PathVariable Long userId) {
        return client.getUserById(userId)
            .thenApply(ResponseEntity::ok)
            .exceptionally(ex -> ResponseEntity
                .status(SERVICE_UNAVAILABLE)
                .build());
    }
}
```

## Implementation Steps

### Step 1: Add Dependencies
```bash
# Add to pom.xml
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>
    <version>2.1.0</version>
</dependency>
```

### Step 2: Create Service Client

```java
@Service
public class PaymentServiceClient {
    
    @CircuitBreaker(
        name = "paymentServiceBreaker",
        fallbackMethod = "paymentFallback"
    )
    public PaymentResponse processPayment(PaymentRequest request) {
        // Call external payment service
        return paymentService.charge(request);
    }
    
    public PaymentResponse paymentFallback(
        PaymentRequest request, 
        Exception ex) {
        // Return pending status instead of failing
        return PaymentResponse.builder()
            .status("PENDING")
            .message("Payment queued: " + ex.getMessage())
            .build();
    }
}
```

### Step 3: Configure in application.yml

```yaml
resilience4j:
  circuitbreaker:
    configs:
      default:
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        failureRateThreshold: 50
        waitDurationInOpenState: 5s
        permittedNumberOfCallsInHalfOpenState: 3
    
    instances:
      paymentServiceBreaker:
        baseConfig: default
        waitDurationInOpenState: 10s
        failureRateThreshold: 40
```

### Step 4: Implement Fallback Logic

```java
// Option 1: Default values
public UserResponse userServiceFallback(Long userId, Exception ex) {
    return UserResponse.builder()
        .id(userId)
        .name("Unknown User")
        .email("unknown@example.com")
        .build();
}

// Option 2: Cached data
public UserResponse userServiceFallback(Long userId, Exception ex) {
    return userCache.getOrDefault(userId, defaultUser());
}

// Option 3: Queued for processing
public PaymentResponse paymentFallback(PaymentRequest req, Exception ex) {
    messageQueue.enqueue(req); // Queue for async processing
    return PaymentResponse.builder()
        .status("QUEUED")
        .message("Processing later")
        .build();
}
```

### Step 5: Enable Actuator Endpoints

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,circuitbreakers
  endpoint:
    health:
      show-details: always
```

## Configuration

### Understanding Configuration Parameters

```yaml
resilience4j:
  circuitbreaker:
    instances:
      myServiceBreaker:
        # --- Failure Detection ---
        slidingWindowSize: 10              # Evaluate last 10 calls
        minimumNumberOfCalls: 5            # Minimum calls before evaluation
        failureRateThreshold: 50           # % failures to open circuit
        slowCallRateThreshold: 50          # % slow calls as failures
        slowCallDurationThreshold: 2s      # Duration threshold for slow
        
        # --- State Transitions ---
        waitDurationInOpenState: 5s        # Time before half-open
        permittedNumberOfCallsInHalfOpenState: 3  # Test calls in half-open
        automaticTransitionFromOpenToHalfOpenEnabled: true
        
        # --- Exception Handling ---
        recordExceptions:
          - java.lang.Exception           # Record these
        ignoreExceptions:
          - java.lang.NullPointerException  # Don't count these
```

### Configuration by Scenario

**Aggressive (Fail Fast)**
```yaml
failureRateThreshold: 30           # Open at 30%
slidingWindowSize: 5               # Small window
minimumNumberOfCalls: 2            # Few calls needed
waitDurationInOpenState: 2s        # Quick recovery test
```

**Conservative (Avoid False Positives)**
```yaml
failureRateThreshold: 60           # Need 60% failure
slidingWindowSize: 20              # Large window
minimumNumberOfCalls: 15           # Many calls needed
waitDurationInOpenState: 30s       # Long recovery test
```

**Balanced (Default)**
```yaml
failureRateThreshold: 50           # 50% failure
slidingWindowSize: 10              # Reasonable window
minimumNumberOfCalls: 5            # Reasonable minimum
waitDurationInOpenState: 5s        # Normal recovery time
```

## Testing

### Unit Test - Circuit Breaker State

```java
@SpringBootTest
public class CircuitBreakerTests {
    
    @Autowired
    private CircuitBreakerRegistry registry;
    
    @Test
    public void testCircuitBreakerClosedState() {
        CircuitBreaker cb = registry.circuitBreaker("myServiceBreaker");
        assertEquals(CLOSED, cb.getState());
    }
    
    @Test
    public void testFallbackInvoked() {
        // Mock service to throw exception
        when(externalService.call()).thenThrow(IOException.class);
        
        // Call multiple times to trigger circuit open
        for (int i = 0; i < 10; i++) {
            try {
                client.callService();
            } catch (Exception e) {
                // Expected
            }
        }
        
        // Verify circuit is open
        CircuitBreaker cb = registry.circuitBreaker("myServiceBreaker");
        assertEquals(OPEN, cb.getState());
    }
}
```

### Integration Test - Recovery

```java
@SpringBootTest
public class CircuitBreakerRecoveryTests {
    
    @Test
    public void testServiceRecovery() throws InterruptedException {
        // Cause failures
        triggerFailures();
        
        // Verify circuit is open
        assertEquals(OPEN, getCircuitBreakerState());
        
        // Fix underlying issue
        mockService.setHealthy(true);
        
        // Wait for transition to half-open
        Thread.sleep(6000); // waitDurationInOpenState
        
        // Make test request
        client.callService();
        
        // Verify circuit recovers to closed
        await().atMost(5, SECONDS)
            .until(() -> getCircuitBreakerState() == CLOSED);
    }
}
```

## Monitoring

### Actuator Endpoints

```bash
# View health with circuit breaker details
curl http://localhost:8080/actuator/health

# View all circuit breakers
curl http://localhost:8080/actuator/circuitbreakers

# View specific circuit breaker details
curl http://localhost:8080/actuator/circuitbreakers/userServiceBreaker

# View metrics
curl http://localhost:8080/actuator/metrics

# View circuit breaker state metric
curl http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.state
```

### Metrics to Monitor

```
resilience4j_circuitbreaker_calls_total
  - Labels: name, kind (SUCCESSFUL, FAILED, IGNORED, SLOW)
  
resilience4j_circuitbreaker_state
  - Value: 0 (CLOSED), 1 (OPEN), 2 (HALF_OPEN)
  
resilience4j_circuitbreaker_buffered_calls
  - Count of calls in buffer

resilience4j_retry_calls_total
  - Total retry attempts
```

### Logging

```java
@Slf4j
@Service
public class MyService {
    
    @CircuitBreaker(name = "myBreaker", fallbackMethod = "fallback")
    public String call() {
        log.info("Calling external service");
        return externalService.get();
    }
    
    public String fallback(Exception e) {
        log.warn("Circuit breaker fallback triggered: {}", e.getMessage());
        return "Default response";
    }
}
```

### Prometheus Integration

Add to pom.xml:
```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

Add to application.yml:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: prometheus
```

Then scrape `http://localhost:8080/actuator/prometheus`

## Troubleshooting

### Problem: Circuit Never Opens

**Causes:**
1. Failure rate not reached
2. Minimum calls threshold not met
3. Exceptions being ignored

**Solutions:**
```yaml
# Lower thresholds
failureRateThreshold: 30
minimumNumberOfCalls: 2

# Check ignored exceptions
recordExceptions:
  - java.lang.Exception
ignoreExceptions: []  # Empty if none should be ignored
```

### Problem: Circuit Doesn't Recover

**Causes:**
1. Wait duration too long
2. Service still unhealthy
3. Automatic transition disabled

**Solutions:**
```yaml
waitDurationInOpenState: 5s  # Reduce wait time
automaticTransitionFromOpenToHalfOpenEnabled: true

# Or manually force recovery
circuitBreaker.transitionToHalfOpen()
```

### Problem: Fallback Not Called

**Causes:**
1. Method signature mismatch
2. Wrong fallback method name
3. Exception type not matching

**Solution:**
```java
// Correct signature
public Response fallback(RequestParam param, Exception ex) {
    return defaultResponse();
}

// Wrong - won't work
public Response fallback(RequestParam param) { }
```

### Problem: High Latency in Half-Open

**Causes:**
1. Too few test requests
2. Service still recovering
3. Short test timeout

**Solutions:**
```yaml
permittedNumberOfCallsInHalfOpenState: 10  # More test calls
slowCallDurationThreshold: 5s              # More lenient timeout
waitDurationInOpenState: 10s               # Longer before testing
```

## Best Practices

✅ **Implement meaningful fallbacks** - not just null
✅ **Use separate breakers per service** - don't share
✅ **Configure appropriate thresholds** - test in staging
✅ **Log fallback invocations** - track degradation
✅ **Monitor circuit state** - watch for frequent opening
✅ **Test recovery scenarios** - ensure system can heal
✅ **Use retry patterns** - handle transient failures
✅ **Combine with timeouts** - prevent hanging
✅ **Document configuration** - explain decisions
✅ **Alert on state changes** - know when circuits open

## Conclusion

The Circuit Breaker pattern with Resilience4j provides:
- Automatic failure detection
- Cascading failure prevention
- Graceful degradation
- Service recovery
- Comprehensive monitoring

Perfect for building resilient microservices!
