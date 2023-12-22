package com.example.model;

import java.io.Serializable;
import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "order_payments")
public class OrderPayment extends TimeEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "type", nullable = false)
	private String type;

	@Column(name = "paid", nullable = false)
	private Double paid;

	@Column(name = "method", nullable = false)
	private String method;

	@Column(name = "paid_at")
	private Timestamp paidAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", referencedColumnName = "id")
	private Order order;

	public OrderPayment(Long id, Double paid, String method) {
		this.id = id;
		this.paid = paid;
		// this.type  = type;
		this.method = method;
		this.order = new Order();
	}

	public OrderPayment() {}

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

	// @Transient
	// private String uploadStatus;
}
