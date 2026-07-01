package com.harsh.order.service;

import com.harsh.common.config.KafkaTopics;
import com.harsh.common.event.InventoryFailedEvent;
import com.harsh.order.entity.Order;
import com.harsh.order.enums.OrderStatus;
import com.harsh.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class InventoryFailedConsumer {

	private final OrderRepository repository;

	@KafkaListener(topics = KafkaTopics.INVENTORY_FAILED, containerFactory = "order-group")
	@Transactional
	public void consume(InventoryFailedEvent event) {

		Order order = repository.findById(event.getOrderId()).orElseThrow();

		order.setStatus(OrderStatus.CANCELLED);
	}
}