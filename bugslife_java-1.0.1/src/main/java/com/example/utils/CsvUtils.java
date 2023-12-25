package com.example.utils;

import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import com.example.OrderDeliveriesDto;

public class CsvUtils {

	public static Resource writeToCsv(List<OrderDeliveriesDto> orderDeliveriesDtos) {
        // CSV形式に変換するロジックを実装
        String csvContent = convertToCsv(orderDeliveriesDtos);
        
        // csvContentをResourceに変換して返す
        ByteArrayResource resource = new ByteArrayResource(csvContent.getBytes());
        return resource;
    }

    public static String convertToCsv(List<OrderDeliveriesDto> orderDeliveriesDtos) {
        StringBuilder csvContent = new StringBuilder("orderId,shippingCode,shippingDate,deliveryDate,deliveryTimezone\n");

		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		for (OrderDeliveriesDto orderDeliveriesDto : orderDeliveriesDtos) {
				csvContent.append(orderDeliveriesDto.getId()).append(",")
							.append(orderDeliveriesDto.getShippingCode()).append(",")
							.append(dateFormatter.format(orderDeliveriesDto.getShippingDate())).append(",")
							.append(dateFormatter.format(orderDeliveriesDto.getDeliveryDate())).append(",")
							.append(orderDeliveriesDto.getDeliveryTimezome()).append("\n");
		}
		return csvContent.toString();
	}
	
}
