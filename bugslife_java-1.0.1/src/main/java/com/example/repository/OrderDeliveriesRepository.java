package com.example.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.model.Order;
import com.example.model.OrderDeliveries;

public interface OrderDeliveriesRepository extends JpaRepository<OrderDeliveries, Long> {
	List<OrderDeliveries> findAllByOrder_Status(String status);
	Optional<OrderDeliveries> findByOrder(Order order);
}
