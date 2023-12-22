package com.example;

import java.time.LocalDate;

import com.example.model.OrderDeliveries;

public class OrderDeliveriesDto {
	private Long orderId;
    private String shippingCode;
    private LocalDate shippingDate;
    private LocalDate deliveryDate;
    private String deliveryTimezone;

	public Long getId(){
		return orderId;
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
		return deliveryTimezone;
	}

	public OrderDeliveriesDto(Long orderId, String shippingCode, LocalDate shippingDate, LocalDate deliveryDate, String deliveryTimezone, boolean checked) {
        this.orderId = orderId;
        this.shippingCode = shippingCode;
        this.shippingDate = shippingDate;
        this.deliveryDate = deliveryDate;
        this.deliveryTimezone = deliveryTimezone;
    }
	public OrderDeliveriesDto(OrderDeliveries orderDeliveries) {
    this.orderId = orderDeliveries.getOrder().getId();
    this.shippingCode = orderDeliveries.getShippingCode();
    this.shippingDate = orderDeliveries.getShippingDate();
    this.deliveryDate = orderDeliveries.getDeliveryDate();
    this.deliveryTimezone = orderDeliveries.getDeliveryTimezome();
	}
}
