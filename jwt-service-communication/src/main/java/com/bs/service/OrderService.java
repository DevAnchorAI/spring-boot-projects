package com.bs.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import com.bs.dto.Order;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OrderService {

    private final Map<Long, Order> ordersStore = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    /**
     * Create a new order
     */
    public Order createOrder(Order order, String username) {
        Long id = idGenerator.getAndIncrement();
        order.setId(id);
        order.setUsername(username);
        order.setStatus("PENDING");
        ordersStore.put(id, order);
        log.info("Order created: {} for user: {}", id, username);
        return order;
    }

    /**
     * Get order by ID
     */
    public Order getOrder(Long id) {
        log.info("Fetching order: {}", id);
        return ordersStore.get(id);
    }

    /**
     * Get all orders for a user
     */
    public List<Order> getUserOrders(String username) {
        log.info("Fetching all orders for user: {}", username);
        List<Order> userOrders = new ArrayList<>();
        ordersStore.values().stream()
                .filter(order -> username.equals(order.getUsername()))
                .forEach(userOrders::add);
        return userOrders;
    }

    /**
     * Update order status
     */
    public Order updateOrder(Long id, Order order) {
        if (ordersStore.containsKey(id)) {
            order.setId(id);
            ordersStore.put(id, order);
            log.info("Order updated: {}", id);
            return order;
        }
        return null;
    }

    /**
     * Delete order
     */
    public boolean deleteOrder(Long id) {
        if (ordersStore.containsKey(id)) {
            ordersStore.remove(id);
            log.info("Order deleted: {}", id);
            return true;
        }
        return false;
    }

    /**
     * Get all orders
     */
    public List<Order> getAllOrders() {
        log.info("Fetching all orders");
        return new ArrayList<>(ordersStore.values());
    }
}
