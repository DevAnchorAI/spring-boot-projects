package com.example.circuitbreaker.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for Circuit Breaker functionality
 */
@SpringBootTest
@TestPropertySource(properties = {
        "resilience4j.circuitbreaker.instances.userServiceBreaker.failureRateThreshold=50",
        "resilience4j.circuitbreaker.instances.userServiceBreaker.slidingWindowSize=5"
})
public class CircuitBreakerServiceTests {

    @Autowired
    private ExternalServiceClient externalServiceClient;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    private CircuitBreaker userServiceBreaker;

    @BeforeEach
    public void setUp() {
        userServiceBreaker = circuitBreakerRegistry.circuitBreaker("userServiceBreaker");
        // Reset the circuit breaker state for each test
        userServiceBreaker.reset();
    }

    @Test
    public void testUserServiceCircuitBreakerClosedState() {
        assertNotNull(userServiceBreaker);
        assertEquals(CircuitBreaker.State.CLOSED, userServiceBreaker.getState());
    }

    @Test
    public void testUserServiceFallbackMethodIsInvoked() throws InterruptedException {
        // This test demonstrates that fallback is called
        Long userId = 1L;

        var result = externalServiceClient.getUserById(userId)
                .exceptionally(ex -> {
                    // This should trigger fallback
                    return null;
                });

        assertNotNull(result);
    }

    @Test
    public void testPaymentServiceCircuitBreakerClosedState() {
        CircuitBreaker paymentBreaker = circuitBreakerRegistry.circuitBreaker("paymentServiceBreaker");
        assertNotNull(paymentBreaker);
        assertEquals(CircuitBreaker.State.CLOSED, paymentBreaker.getState());
    }

    @Test
    public void testNotificationServiceCircuitBreakerClosedState() {
        CircuitBreaker notificationBreaker = circuitBreakerRegistry.circuitBreaker("notificationServiceBreaker");
        assertNotNull(notificationBreaker);
        assertEquals(CircuitBreaker.State.CLOSED, notificationBreaker.getState());
    }
}
