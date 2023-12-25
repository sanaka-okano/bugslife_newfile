package com.example.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStatus(String string);
    Optional<Order> getOrderById(Long orderId);
    @Query(value = "SELECT * FROM orders", nativeQuery = true) // SQL
    List<Order> findOrdersList();
}
