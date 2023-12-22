package com.example.form;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

import com.example.model.OrderDeliveries;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class OrderForm {

	@Setter
	@Getter
	@NoArgsConstructor
	public static class Product {
		private Long productId;

		private Integer quantity = 1;

		private Double discount = 0.0;
	}

	@Setter
	@Getter
	@NoArgsConstructor
	public static class Create {

		private Integer customerId;

		private Double shipping = 0.0;

		private String note;

		private String paymentMethod;

		private List<Product> orderProducts;
	}

	@Setter
	@Getter
	@NoArgsConstructor
	public static class Update {

		private Long id;

		private Double shipping;

		private String note;

		private String paymentMethod;

		private String status;
	}

	@Setter
	@Getter
	@NoArgsConstructor
	public static class UpdateProduct {
		private Long id;

		private List<Product> orderProducts;

		/**
		 * 税金関連の情報をアップデートするかどうか
		 *
		 * @descreption このフラグがtrueの場合、同一商品の税金関連の情報をアップデートする
		 */
		private Boolean updateTax;
	}

	@Setter
	@Getter
	@NoArgsConstructor
	public static class UpdateStatus {
		private Long id;

		private String status;
	}

	@Setter
	@Getter
	@NoArgsConstructor
	public static class CreatePayment {
		private Long orderId;

		private String type;

		private Double paid;

		private Timestamp paidAt;

		private String method;
	}

	@Data
	public class OrderShippingData {
		private List<OrderDeliveries> orderShippingList;

		private Set<Long> checkedItems;

		private String uploadStatus;

		@Override
		public String toString() {
			return "OrderShippingData{" +
					"orderShippingList=" + orderShippingList +
					", checkedItems=" + checkedItems +
					", uploadStatus='" + uploadStatus + '\'' +
					'}';
		}
	}
}
