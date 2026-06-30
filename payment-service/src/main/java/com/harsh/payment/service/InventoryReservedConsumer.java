package com.harsh.payment.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.harsh.common.config.KafkaTopics;
import com.harsh.common.event.InventoryReservedEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InventoryReservedConsumer {

	private final PaymentService paymentService;

	@KafkaListener(topics = KafkaTopics.INVENTORY_RESERVED, groupId = "payment-group")
	public void consume(InventoryReservedEvent event) {

		paymentService.processPayment(event);

	}

}