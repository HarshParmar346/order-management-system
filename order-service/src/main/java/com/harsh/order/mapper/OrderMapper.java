package com.harsh.order.mapper;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.harsh.order.dto.OrderRequest;
import com.harsh.order.entity.Order;
import com.harsh.order.enums.OrderStatus;

@Component
public class OrderMapper {

	public Order toEntity(OrderRequest request) {

		return Order.builder().customerId(request.getCustomerId()).productId(request.getProductId())
				.quantity(request.getQuantity()).status(OrderStatus.CREATED).createdAt(LocalDateTime.now()).build();
	}

}