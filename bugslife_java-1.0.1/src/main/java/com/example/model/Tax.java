package com.example.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "taxs")
public class Tax {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "taxsrate", nullable = false)
	private int rate;

	@Column(name = "taxincluded", nullable = false)
	private boolean taxIncluded;

	@Column(name = "rounding", nullable = false)
	private String rounding;

	public void setRate(int rate) {
		this.rate = rate;
	}

	public int getRate() {
		return rate;
	}

	public void setTaxIncluded(boolean taxIncluded){
		this.taxIncluded = taxIncluded;
	}

	public boolean getTaxIncluded(){
		return taxIncluded;
	}

	public void setRounding(String rounding){
		this.rounding = rounding;
	}

	public String getRounding(){
		return rounding;
	}

	public Tax(int rate, boolean taxIncluded, String rounding) {
        this.rate = rate;
        this.taxIncluded = taxIncluded;
        this.rounding = rounding;
    }

	public Tax(int id, int rate, boolean taxIncluded, String rounding) {
        this.id = (long) id;
        this.rate = rate;
        this.taxIncluded = taxIncluded;
        this.rounding = rounding;
    }

}
