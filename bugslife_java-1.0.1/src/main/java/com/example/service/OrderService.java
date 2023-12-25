package com.example.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

// import org.hibernate.engine.internal.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.constants.TaxType;
import com.example.enums.OrderStatus;
import com.example.enums.PaymentStatus;
import com.example.form.OrderForm;
import com.example.form.OrderForm.OrderPaymentData;
import com.example.model.Order;
import com.example.model.OrderDeliveries;
import com.example.model.OrderPayment;
import com.example.model.OrderProduct;
import com.example.repository.OrderPaymentRepository;
import com.example.repository.OrderRepository;
import com.example.repository.ProductRepository;

@Service
@Transactional(readOnly = true)
public class OrderService {

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private ProductRepository productRepository;


	@Autowired
	private OrderPaymentRepository orderPaymentRepository;
	
	private final NamedParameterJdbcTemplate jdbcTemplate;

	public OrderService(NamedParameterJdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}


	public List<Order> findAll() {
		return orderRepository.findAll();
	}

	public Optional<Order> findOne(Long id) {
		return orderRepository.findById(id);
	}

	@Transactional(readOnly = false)
	public Order save(Order entity) {
		return orderRepository.save(entity);
	}

	public List<OrderDeliveries> getOrderDeliveriesByOrderId(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);

        if (order != null) {
            return order.getOrderDeliveriesList();
        } else {
            return Collections.emptyList();
        }
    }

	@Transactional(readOnly = false)
	public Order create(OrderForm.Create entity) {
		Order order = new Order();
		order.setCustomerId(entity.getCustomerId());
		order.setShipping(entity.getShipping());
		order.setNote(entity.getNote());
		order.setPaymentMethod(entity.getPaymentMethod());
		order.setStatus(OrderStatus.ORDERED);
		order.setPaymentStatus(PaymentStatus.UNPAID);
		order.setPaid(0.0);

		var orderProducts = new ArrayList<OrderProduct>();
		entity.getOrderProducts().forEach(p -> {
			var product = productRepository.findById(p.getProductId()).get();
			var orderProduct = new OrderProduct();
			orderProduct.setProductId(product.getId());
			orderProduct.setCode(product.getCode());
			orderProduct.setName(product.getName());
			orderProduct.setQuantity(p.getQuantity());
			orderProduct.setPrice((double)product.getPrice());
			orderProduct.setDiscount(p.getDiscount());
			orderProduct.setTaxType(TaxType.get(product.getTaxType()));
			orderProducts.add(orderProduct);
		});

		// 計算
		var total = 0.0;
		var totalTax = 0.0;
		var totalDiscount = 0.0;
		for (var orderProduct : orderProducts) {
			var price = orderProduct.getPrice();
			var quantity = orderProduct.getQuantity();
			var discount = orderProduct.getDiscount();
			var tax = 0.0;
			/**
			 * 税額を計算する
			 */
			if (orderProduct.getTaxIncluded()) {
				// 税込みの場合
				tax = price * quantity * orderProduct.getTaxRate() / (100 + orderProduct.getTaxRate());
			} else {
				// 税抜きの場合
				tax = price * quantity * orderProduct.getTaxRate() / 100;
			}
			// 端数処理
			tax = switch (orderProduct.getTaxRounding()) {
			case TaxType.ROUND -> Math.round(tax);
			case TaxType.CEIL -> Math.ceil(tax);
			case TaxType.FLOOR -> Math.floor(tax);
			default -> tax;
			};
			var subTotal = price * quantity + tax - discount;
			total += subTotal;
			totalTax += tax;
			totalDiscount += discount;
		}
		order.setTotal(total);
		order.setTax(totalTax);
		order.setDiscount(totalDiscount);
		order.setGrandTotal(total + order.getShipping());
		order.setOrderProducts(orderProducts);

		orderRepository.save(order);

		return order;

	}

	@Transactional()
	public void delete(Order entity) {
		orderRepository.delete(entity);
	}

	@Transactional(readOnly = false)
	public void createPayment(OrderForm.CreatePayment entity) {
		var order = orderRepository.findById(entity.getOrderId()).get();
		/**
		 * 新しい支払い情報を登録する
		 */
		var payment = new OrderPayment();
		payment.setType(entity.getType());
		payment.setPaid(entity.getPaid());
		payment.setMethod(entity.getMethod());
		payment.setPaidAt(entity.getPaidAt());

		/**
		 * 支払い情報を更新する
		 */
		// orderのorderPaymentsに追加
		order.getOrderPayments().add(payment);
		// 支払い済み金額を計算
		var paid = order.getOrderPayments().stream().mapToDouble(p -> p.getPaid()).sum();
		// 合計金額から支払いステータスを判定
		var paymentStatus = paid > order.getGrandTotal() ? PaymentStatus.OVERPAID
				: paid < order.getGrandTotal() ? PaymentStatus.PARTIALLY_PAID : PaymentStatus.PAID;

		// 更新
		order.setPaid(paid);
		order.setPaymentStatus(paymentStatus);
		orderRepository.save(order);
	}

		@Transactional(readOnly = false)
		public OrderPaymentData getOrderPaymentData(){
			List<OrderPayment> orderPaymentList = create();
			OrderForm orderForm = new OrderForm();
			OrderForm.OrderPaymentData orderPaymentData = orderForm.new OrderPaymentData();
			orderPaymentData.setOrderPaymentList(orderPaymentList);
			return orderPaymentData;
		}

		@Transactional(readOnly = false)
		public List<OrderPayment> create(){
			List<OrderPayment> orderPayment = orderPaymentRepository.findAll();
			List<Order> orders = orderRepository.findAll();
			for(Order order : orders){
				if(!containsOrderId(orderPayment, order.getId())){
					OrderPayment orderpaid = new OrderPayment();
					orderpaid.setOrderId(order.getId());
					orderpaid.setPaid(order.getPaid());
					orderpaid.setMethod(order.getPaymentMethod());
					orderpaid.setType(order.getPaymentStatus());
					orderPaymentRepository.save(orderpaid);
					orderPayment.add(orderpaid);
				}
			}
			return orderPayment;
		}

		@Transactional(readOnly = false)
		public boolean containsOrderId(List<OrderPayment> orderPaymentList, Long orderId){
			for (OrderPayment orderPayment : orderPaymentList) {
				if (orderPayment.getOrderId().equals(orderId)) {
					return true;
				}
			}
			return false;
		}


		// *CSVインポート処理**

	// @param file
	// * @throws IOException
	// */
	@Transactional(readOnly = false)
	public void importCSV(MultipartFile file) throws IOException {
		List<OrderPayment> paymentList = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
			String line = br.readLine();
			while ((line = br.readLine()) != null) {
				final String[] split = line.replace("\"", "").split(",");
				final OrderPayment orderPayment = new OrderPayment(
						Long.valueOf(split[0]),
						Double.valueOf(split[1]),
						split[2]);
						
				paymentList.add(orderPayment);
			}
		} catch (IOException e) {
			throw new RuntimeException("ファイルが読み込めません", e);
		}
		batchUpdate(paymentList);
	}

	@SuppressWarnings("unused")
	private int[] batchUpdate(List<OrderPayment> orderPayment) {
		try {
			String sql = "UPDATE order_payments SET "
					+ "paid = :paid, "
					+ "method = :method "
					+ "WHERE order_id = :order_id";

			SqlParameterSource[] batchArgs = orderPayment.stream()
					.map((OrderPayment o) -> {
						MapSqlParameterSource map = new MapSqlParameterSource();
						map.addValue("order_id", o.getId(), Types.INTEGER);
						map.addValue("paid", o.getPaid(), Types.DOUBLE);
						// map.addValue("type", o.getType(), Types.VARCHAR);
						map.addValue("method", o.getMethod(), Types.VARCHAR);
						return map;
					})
					.toArray(SqlParameterSource[]::new);

			return jdbcTemplate.batchUpdate(sql, batchArgs);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("バッチ更新エラー");
			}
	}

	@Transactional(readOnly = false)
	public void updateOrderPaymentStatus(Long orderId, String paymentStatus) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new RuntimeException("Order not found"));
	
		order.setPaymentStatus(paymentStatus);
		orderRepository.save(order);
	}
	@Transactional(readOnly = false)
	public void updateOrderPaymentType(Long orderId, String type) {
		// OrderPaymentエンティティが存在するか確認し、存在する場合は更新
		Optional<OrderPayment> orderPaymentOptional = orderPaymentRepository.findByOrder_Id(orderId);
		if(orderPaymentOptional.isPresent()){
			OrderPayment orderPayment = orderPaymentOptional.get();
			orderPayment.setType(type);
			orderPaymentRepository.save(orderPayment);
		}else{
			System.out.println("orderPayment" + orderPaymentOptional);
		}
	}
	@Transactional(readOnly = false)
	public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }
	



}
