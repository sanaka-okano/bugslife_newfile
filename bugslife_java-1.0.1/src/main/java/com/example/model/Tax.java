package com.example.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "taxs")
public class Tax {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "taxsrate", nullable = false)
	private Double rate;

	@Column(name = "taxincluded", nullable = false)
	private Boolean taxIncluded;

	@Column(name = "rounding", nullable = false)
	private String rounding;

	public void setRate(Double rate) {
		this.rate = rate;
	}

	public Double getRate() {
		return rate;
	}

}
