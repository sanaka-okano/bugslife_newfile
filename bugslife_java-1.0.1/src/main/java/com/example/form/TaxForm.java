package com.example.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TaxForm {

	private long id;

	@NotBlank(message = "税率を入力してください")
	private int rate;

	private Boolean taxIncluded;

	private String rounding;

	/**
	 * Rounding
	 */
	public static final String FLOOR = "floor";
	public static final String ROUND = "round";
	public static final String CEIL = "ceil";




}