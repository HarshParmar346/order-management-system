package com.harsh.order.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.harsh.common.config.KafkaTopics;
import com.harsh.common.event.PaymentFailedEvent;
import com.harsh.order.entity.Order;
import com.harsh.order.enums.OrderStatus;
import com.harsh.order.repository.OrderRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentFailedConsumer {

	private final OrderRepository repository;

	@KafkaListener(topics = KafkaTopics.PAYMENT_FAILED, groupId = "order-group")
	@Transactional
	public void consume(PaymentFailedEvent event) {

		Order order = repository.findById(event.getOrderId()).orElseThrow();

		order.setStatus(OrderStatus.FAILED);

	}

}