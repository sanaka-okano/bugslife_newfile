package com.example.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.model.OrderDeliveries;

public interface OrderDeliveriesRepository extends JpaRepository<OrderDeliveries, Long> {
	List<OrderDeliveries> findAllByOrder_Status(String status);
}
