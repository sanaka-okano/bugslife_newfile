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
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "order_deliveries")
public class OrderDeliveries {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "shipping_code")
	private String shippingCode;

	@Column(name = "shipping_date")
	private LocalDate shippingDate;

	@Column(name = "delivery_date")
	private LocalDate deliveryDate;

	@Column(name = "delivery_timezome")
	private String deliveryTimezome;

	@Transient
	private boolean checked;

	@Transient
	private String uploadStatus;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id")
	private Order order;

	public OrderDeliveries(Long id, String shippingCode, LocalDate shippingDate, LocalDate deliveryDate, String deliveryTimezome){
		this.id = id;
		this.shippingCode = shippingCode;
		this.shippingDate  = shippingDate;
		this.deliveryDate = deliveryDate;
		this.deliveryTimezome = deliveryTimezome;
		this.order = new Order();
	}

	public void setOrderDeliveries(){
		this.deliveryDate = null;
		this.deliveryTimezome = null;
	}

	public void setOrderId(Long orderId) {
		if(orderId != null){
			if (order == null) {
				order = new Order();
			}
			order.setId(orderId);
		}
    }

	public Long getOrderId(){
		return order.getId();
	}

	public boolean getIsCheckes(){
		return checked;
	}

}
