package com.harsh.inventory.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.harsh.common.config.KafkaTopics;
import com.harsh.common.event.InventoryFailedEvent;
import com.harsh.common.event.InventoryReservedEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryProducer {

	private final KafkaTemplate<String, Object> kafkaTemplate;

	public void publishReserved(InventoryReservedEvent event) {

		kafkaTemplate.send(KafkaTopics.INVENTORY_RESERVED, event.getOrderId().toString(), event);

	}

	public void publishFailed(InventoryFailedEvent event) {

		kafkaTemplate.send(KafkaTopics.INVENTORY_FAILED, event.getOrderId().toString(), event);

	}

}