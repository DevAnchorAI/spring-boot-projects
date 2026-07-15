package com.bs.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bs.dto.Order;
import com.bs.service.JwtTokenService;
import com.bs.service.OrderService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;
    private final JwtTokenService jwtTokenService;

    public OrderController(OrderService orderService, JwtTokenService jwtTokenService) {
        this.orderService = orderService;
        this.jwtTokenService = jwtTokenService;
    }

    /**
     * Create a new order (requires JWT token)
     * Request header: Authorization: Bearer <jwt_token>
     */
    @PostMapping("/create")
    public ResponseEntity<?> createOrder(
            @RequestBody Order order,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        try {
            String token = extractToken(authHeader);
            if (token == null || !jwtTokenService.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid or missing JWT token");
            }

            // In production, extract username from JWT token
            // For now, use the username from order object
            String username = order.getUsername() != null ? order.getUsername() : "anonymous";
            Order createdOrder = orderService.createOrder(order, username);

            return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
        } catch (Exception e) {
            log.error("Error creating order: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating order: " + e.getMessage());
        }
    }

    /**
     * Get order by ID (requires JWT token)
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        try {
            String token = extractToken(authHeader);
            if (token == null || !jwtTokenService.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid or missing JWT token");
            }

            Order order = orderService.getOrder(id);
            if (order != null) {
                return ResponseEntity.ok(order);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Order not found");
            }
        } catch (Exception e) {
            log.error("Error fetching order: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching order: " + e.getMessage());
        }
    }

    /**
     * Get all orders for a user (requires JWT token)
     */
    @GetMapping("/user/orders")
    public ResponseEntity<?> getUserOrders(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        try {
            String token = extractToken(authHeader);
            if (token == null || !jwtTokenService.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid or missing JWT token");
            }

            // In production, extract username from JWT token
            String username = "anonymous";
            List<Order> orders = orderService.getUserOrders(username);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Error fetching user orders: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching orders: " + e.getMessage());
        }
    }

    /**
     * Update order (requires JWT token)
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrder(
            @PathVariable Long id,
            @RequestBody Order order,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        try {
            String token = extractToken(authHeader);
            if (token == null || !jwtTokenService.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid or missing JWT token");
            }

            Order updatedOrder = orderService.updateOrder(id, order);
            if (updatedOrder != null) {
                return ResponseEntity.ok(updatedOrder);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Order not found");
            }
        } catch (Exception e) {
            log.error("Error updating order: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating order: " + e.getMessage());
        }
    }

    /**
     * Delete order (requires JWT token)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrder(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        try {
            String token = extractToken(authHeader);
            if (token == null || !jwtTokenService.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid or missing JWT token");
            }

            boolean deleted = orderService.deleteOrder(id);
            if (deleted) {
                return ResponseEntity.ok("Order deleted successfully");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Order not found");
            }
        } catch (Exception e) {
            log.error("Error deleting order: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting order: " + e.getMessage());
        }
    }

    /**
     * Get all orders (requires JWT token)
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllOrders(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        try {
            String token = extractToken(authHeader);
            if (token == null || !jwtTokenService.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid or missing JWT token");
            }

            List<Order> orders = orderService.getAllOrders();
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Error fetching all orders: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching orders: " + e.getMessage());
        }
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
