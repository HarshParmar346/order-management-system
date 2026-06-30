package com.harsh.inventory.service;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.harsh.common.event.InventoryFailedEvent;
import com.harsh.common.event.InventoryReservedEvent;
import com.harsh.common.event.OrderCreatedEvent;
import com.harsh.inventory.entity.Inventory;
import com.harsh.inventory.repository.InventoryRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryServiceImpl implements InventoryService {

	private final InventoryRepository repository;

	private final InventoryProducer producer;

	private final RedisTemplate<String, Object> redisTemplate;

	@Override
	public void reserveInventory(OrderCreatedEvent event) {

		String lockKey = "inventory:" + event.getProductId();

		Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", Duration.ofSeconds(5));

		if (Boolean.FALSE.equals(locked)) {

			return;

		}

		try {

			Inventory inventory = repository.findById(event.getProductId()).orElseThrow();

			if (inventory.getQuantity() < event.getQuantity()) {

				producer.publishFailed(

						InventoryFailedEvent.builder()

								.metadata(event.getMetadata())

								.orderId(event.getOrderId())

								.reason("Insufficient stock")

								.build()

				);

				return;

			}

			inventory.setQuantity(

					inventory.getQuantity() - event.getQuantity()

			);

			repository.save(inventory);

			producer.publishReserved(

					InventoryReservedEvent.builder()

							.metadata(event.getMetadata())

							.orderId(event.getOrderId())

							.productId(event.getProductId()).reservedQuantity(event.getQuantity())
							.amount(event.getAmount()).build()

			);

		}

		finally {

			redisTemplate.delete(lockKey);

		}

	}

}