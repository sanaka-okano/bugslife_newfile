package com.example.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.model.OrderPayment;

public interface OrderPaymentRepository extends JpaRepository<OrderPayment, Long> {
		Optional<OrderPayment> findByOrder_Id(Long order);
		

}
