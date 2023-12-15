package com.example.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Types;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.model.OrderDeliveries;
import com.example.repository.OrderDeliveriesRepository;

@Service
public class OrderDeliveriesService {
	
	@Autowired
	private OrderDeliveriesRepository orderDeliveriesRepository;

	private final NamedParameterJdbcTemplate jdbcTemplate;

	public OrderDeliveriesService(NamedParameterJdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<OrderDeliveries> findAll(){
		System.out.println("--------------------------");
		return orderDeliveriesRepository.findAll();
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
				final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/d");
				final OrderDeliveries orderDeliveries = new OrderDeliveries(
						Long.valueOf(split[0]),
						split[1],
						LocalDate.parse(split[2], formatter),
						LocalDate.parse(split[3], formatter),
						split[4]);
				shippingList.add(orderDeliveries);
			}
		} catch (IOException e) {
			throw new RuntimeException("ファイルが読み込めません", e);
		}
		batchInsert(shippingList);
	}

	@SuppressWarnings("unused")
	private int[] batchInsert(List<OrderDeliveries> orderDeliveries) {
		try {
			String sql = "INSERT INTO order_deliveries  (order_id, shipping_code, shipping_date, delivery_date, delivery_timezome)"
					+ " VALUES (:order_id, :shipping_code, :shipping_date, :delivery_date, :delivery_timezome)";
			SqlParameterSource[] batchArgs = orderDeliveries.stream()
					.map(o -> new MapSqlParameterSource()
							.addValue("order_id", o.getId(), Types.INTEGER)
							.addValue("shipping_code", o.getShippingCode(), Types.VARCHAR)
							.addValue("shipping_date", o.getShippingDate(), Types.DATE)
							.addValue("delivery_date", o.getDeliveryDate(), Types.DATE)
							.addValue("delivery_timezome", o.getDeliveryTimezome(), Types.VARCHAR)
					)
					.toArray(SqlParameterSource[]::new);
			return jdbcTemplate.batchUpdate(sql, batchArgs);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("バッチ挿入エラー");
		}
	}
}


