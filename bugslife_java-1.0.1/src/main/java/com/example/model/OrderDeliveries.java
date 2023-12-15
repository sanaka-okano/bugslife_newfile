package com.example.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_deliveries")
public class OrderDeliveries {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "order_id")
	private Long id;

	@Column(name = "shipping_code")
	private String shippingCode;

	@Column(name = "shipping_date")
	private LocalDate shippingDate;

	@Column(name = "delivery_date")
	private LocalDate deliveryDate;

	@Column(name = "delivery_timezome")
	private String deliveryTimezome;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", insertable = false, updatable = false)
	private Order order;

	public OrderDeliveries(Long id, String shippingCode, LocalDate shippingDate, LocalDate deliveryDate, String deliveryTimezome){
		this.id = id;
		this.shippingCode = shippingCode;
		this.shippingDate  = shippingDate;
		this.deliveryDate = deliveryDate;
		this.deliveryTimezome = deliveryTimezome;
	}

	public Long getId(){
		return id;
	}

	public String getShippingCode() {
        return shippingCode;
    }
	public LocalDate getShippingDate() {
        return shippingDate;
    }

	public LocalDate getDeliveryDate(){
		return deliveryDate;
	}
	public String getDeliveryTimezome(){
		return deliveryTimezome;
	}
}
