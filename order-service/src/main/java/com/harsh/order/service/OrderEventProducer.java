package com.harsh.order.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.harsh.common.config.KafkaTopics;
import com.harsh.common.event.OrderCreatedEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderEventProducer {

	private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

	public void publish(OrderCreatedEvent event) {

		kafkaTemplate.send(KafkaTopics.ORDER_CREATED, event.getOrderId().toString(), event);

	}

}