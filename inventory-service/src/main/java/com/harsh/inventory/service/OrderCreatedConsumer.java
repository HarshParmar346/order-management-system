package com.harsh.inventory.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.harsh.common.config.KafkaTopics;
import com.harsh.common.event.OrderCreatedEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderCreatedConsumer {

	private final InventoryService inventoryService;

	@KafkaListener(topics = KafkaTopics.ORDER_CREATED, groupId = "inventory-group")
	public void consume(OrderCreatedEvent event) {

		inventoryService.reserveInventory(event);

	}

}