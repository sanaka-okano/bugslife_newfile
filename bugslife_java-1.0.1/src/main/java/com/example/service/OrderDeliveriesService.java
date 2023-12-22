package com.example.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Types;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
// import org.hibernate.engine.internal.Collections;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.OrderDeliveriesDto;
import com.example.form.OrderForm;
import com.example.form.OrderForm.OrderShippingData;
import com.example.model.Order;
import com.example.model.OrderDeliveries;
import com.example.repository.OrderDeliveriesRepository;
import com.example.repository.OrderRepository;
import com.example.utils.CsvUtils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class OrderDeliveriesService {
	
	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private OrderDeliveriesRepository orderDeliveriesRepository;

	private final NamedParameterJdbcTemplate jdbcTemplate;

	@PersistenceContext
    private EntityManager entityManager;

	public OrderDeliveriesService(NamedParameterJdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<OrderDeliveries> findAll(){
		System.out.println("--------------------------");
		return orderDeliveriesRepository.findAll();
	}

	public List<OrderDeliveries> addOrderDeliveriesForOrderedOrders() {
		List<OrderDeliveries> orderDeliveriesList = orderDeliveriesRepository.findAll();
		// order テーブルからステータスが "ordered" の全てのオーダーを取得
		List<Order> orderedOrders = orderRepository.findByStatus("ordered");
		// 取得したオーダーに対応する OrderDeliveries エントリを生成して order_deliveries テーブルに挿入
		for (Order orderedOrder : orderedOrders) {
			if(!containsOrderId(orderDeliveriesList, orderedOrder.getId())){
			OrderDeliveries orderShipping = new OrderDeliveries();
			orderShipping.setOrderId(orderedOrder.getId());
			orderShipping.setShippingCode("A");
			orderShipping.setShippingDate(null);
			orderShipping.setChecked(false);
			orderShipping.setUploadStatus("");
			orderShipping.setOrderDeliveries();
			//テーブルいれる
			orderDeliveriesRepository.save(orderShipping);
			orderDeliveriesList.add(orderShipping);
			}else{
				return orderDeliveriesList;
			}
		}
		System.out.println(orderDeliveriesList);
		return new ArrayList<>(orderDeliveriesList);
	}

	// 同じ orderId がリスト内に存在するかを確認するメソッド
private boolean containsOrderId(List<OrderDeliveries> orderDeliveriesList, Long orderId) {
    for (OrderDeliveries orderDeliveries : orderDeliveriesList) {
        if (orderDeliveries.getOrderId().equals(orderId)) {
            return true; // 同じ orderId が存在する場合は true を返す
        }
    }
    return false; // 同じ orderId が存在しない場合は false を返す
}

	public OrderShippingData getOrderShippingData() {
        List<OrderDeliveries> orderShippingList = addOrderDeliveriesForOrderedOrders();
		OrderForm orderForm = new OrderForm();
        OrderForm.OrderShippingData orderShippingData = orderForm.new OrderShippingData();
        orderShippingData.setOrderShippingList(orderShippingList);
		// System.out.println("-----------orderShippingData contents: " + orderShippingData);
        return orderShippingData;
    }


public List<OrderDeliveries> getOrderDeliveriesByOrderId(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);

        if (order != null) {
            return order.getOrderDeliveriesList();
        } else {
            // Order が存在しない場合は空のリストを返す
            return Collections.emptyList();
        }
    }


	public List<OrderDeliveries> getOrderedStatus() {
        return orderDeliveriesRepository.findAllByOrder_Status("ordered");
    }


	// *CSVインポート処理**

	// @param file
	// * @throws IOException
	// */
	@Transactional(readOnly = false)
	public void importCSV(MultipartFile file) throws IOException {
		List<OrderDeliveries> shippingList = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
			String line = br.readLine();
			while ((line = br.readLine()) != null) {
				final String[] split = line.replace("\"", "").split(",");

				validateDataLine(split);
				final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/d");
				final OrderDeliveries orderDeliveries = new OrderDeliveries(
						Long.valueOf(split[0]),
						split[1],
						LocalDate.parse(split[2].trim(), formatter),
						LocalDate.parse(split[3].trim(), formatter),
						split[4]);
						
				shippingList.add(orderDeliveries);
			}
		} catch (IOException e) {
			throw new RuntimeException("ファイルが読み込めません", e);
		}
		batchUpdate(shippingList);
	}

	//バリデーション
	private void validateDataLine(String[] dataLine) throws RuntimeException{
		if (dataLine.length != 5) {
			throw new RuntimeException("Invalid CSV file format. Incorrect number of columns in data line.");
		}
		try {
			// 各列のデータ型を確認
			Long orderId = Long.valueOf(dataLine[0]);
			String shippingCode = String.valueOf(dataLine[1]);
			LocalDate shippingDate = LocalDate.parse(dataLine[2].trim(), DateTimeFormatter.ofPattern("yyyy/MM/d"));
			LocalDate deliveryDate = LocalDate.parse(dataLine[3].trim(), DateTimeFormatter.ofPattern("yyyy/MM/d"));
			String deliveryTimezome = String.valueOf(dataLine[4]);
		} catch (NumberFormatException e){
			throw new RuntimeException("Invalid CSV file format. Failed to parse data line." + ((Throwable) e).getMessage(), (Throwable) e);
		} catch(DateTimeParseException e){
			throw new RuntimeException("Invalid CSV file format. Failed to parse data line." + ((Throwable) e).getMessage(), (Throwable) e);
		}
	}

	//インサート
	@SuppressWarnings("unused")
	private int[] batchInsert(List<OrderDeliveries> orderDeliveries) {
		try {
			String sql = "INSERT INTO order_deliveries  (order_id, shipping_code, shipping_date, delivery_date, delivery_timezome)"
					+ " VALUES (:order_id, :shipping_code, :shipping_date, :delivery_date, :delivery_timezome)";
			SqlParameterSource[] batchArgs = orderDeliveries.stream()
							.map((OrderDeliveries o) -> {
			MapSqlParameterSource map = new MapSqlParameterSource();
			map.addValue("order_id", o.getId(), Types.INTEGER);
			map.addValue("shipping_code", o.getShippingCode(), Types.VARCHAR);
			map.addValue("shipping_date", o.getShippingDate(), Types.DATE);
			map.addValue("delivery_date", o.getDeliveryDate(), Types.DATE);
			map.addValue("delivery_timezome", o.getDeliveryTimezome(), Types.VARCHAR);
			return map;
		})
					.toArray(SqlParameterSource[]::new);
			return jdbcTemplate.batchUpdate(sql, batchArgs);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("バッチ挿入エラー");
		}
	}

	@SuppressWarnings("unused")
private int[] batchUpdate(List<OrderDeliveries> orderDeliveries) {
    try {
        String sql = "UPDATE order_deliveries SET "
                + "shipping_code = :shipping_code, "
                + "shipping_date = :shipping_date, "
                + "delivery_date = :delivery_date, "
                + "delivery_timezome = :delivery_timezome "
                + "WHERE order_id = :order_id";

        SqlParameterSource[] batchArgs = orderDeliveries.stream()
                .map((OrderDeliveries o) -> {
                    MapSqlParameterSource map = new MapSqlParameterSource();
                    map.addValue("order_id", o.getId(), Types.INTEGER);
                    map.addValue("shipping_code", o.getShippingCode(), Types.VARCHAR);
                    map.addValue("shipping_date", o.getShippingDate(), Types.DATE);
                    map.addValue("delivery_date", o.getDeliveryDate(), Types.DATE);
                    map.addValue("delivery_timezome", o.getDeliveryTimezome(), Types.VARCHAR);
                    return map;
                })
                .toArray(SqlParameterSource[]::new);

        return jdbcTemplate.batchUpdate(sql, batchArgs);
    } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException("バッチ更新エラー");
    }
}


	public List<OrderDeliveriesDto> getNotShippedOrders() {
        List<OrderDeliveries> notShippedOrderDeliveries = orderDeliveriesRepository.findAllByOrder_Status("ordered");
    return notShippedOrderDeliveries.stream()
            .map(OrderDeliveriesDto::new)
            .collect(Collectors.toList());
    }

    public Resource generateNotShippedOrdersCsv() {
        List<OrderDeliveriesDto> notShippedOrders = getNotShippedOrders();
        return CsvUtils.writeToCsv(notShippedOrders);
    }

	public void updateShippingList(OrderDeliveries orderShipping) {
		OrderDeliveries existingOrderShipping = orderDeliveriesRepository.findByOrder(orderShipping.getOrder())
			.orElseThrow(() -> new RuntimeException("指定された出荷情報が見つかりませんでした。"));
		try{
			existingOrderShipping.getOrder().setStatus("shipped");
			existingOrderShipping.setUploadStatus("success");
			orderDeliveriesRepository.save(existingOrderShipping);
		} catch(Exception e){
			orderShipping.setUploadStatus("error");
			orderDeliveriesRepository.save(existingOrderShipping);

		}
	}

}


