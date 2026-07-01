package com.harsh.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.harsh.common.dto.EventMetadata;
import com.harsh.common.event.InventoryFailedEvent;
import com.harsh.common.event.InventoryReservedEvent;
import com.harsh.common.event.OrderCreatedEvent;
import com.harsh.inventory.entity.Inventory;
import com.harsh.inventory.repository.InventoryRepository;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

	@Mock
	private InventoryRepository repository;

	@Mock
	private InventoryProducer producer;

	@Mock
	private RedisTemplate<String, Object> redisTemplate;

	@Mock
	private ValueOperations<String, Object> valueOperations;

	private InventoryServiceImpl inventoryService;

	@BeforeEach
	void setUp() {
		inventoryService = new InventoryServiceImpl(repository, producer, redisTemplate);
	}

	private OrderCreatedEvent orderCreatedEvent(long orderId, long productId, int quantity) {

		return OrderCreatedEvent.builder()
				.metadata(EventMetadata.builder().eventId(UUID.randomUUID()).createdAt(LocalDateTime.now()).version(1)
						.build())
				.orderId(orderId).customerId(1L).productId(productId).quantity(quantity)
				.amount(new java.math.BigDecimal(50)).build();
	}

	@Test
	void reserveInventory_decrementsStockAndPublishesReservedEventWhenStockIsSufficient() {

		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.setIfAbsent(anyString(), eq("LOCKED"), any(Duration.class))).thenReturn(true);

		Inventory inventory = Inventory.builder().productId(100L).quantity(10).build();
		when(repository.findById(100L)).thenReturn(Optional.of(inventory));

		OrderCreatedEvent event = orderCreatedEvent(1L, 100L, 3);

		inventoryService.reserveInventory(event);

		ArgumentCaptor<Inventory> savedCaptor = ArgumentCaptor.forClass(Inventory.class);
		verify(repository).save(savedCaptor.capture());
		assertThat(savedCaptor.getValue().getQuantity()).isEqualTo(7);

		ArgumentCaptor<InventoryReservedEvent> reservedCaptor = ArgumentCaptor.forClass(InventoryReservedEvent.class);
		verify(producer).publishReserved(reservedCaptor.capture());
		assertThat(reservedCaptor.getValue().getOrderId()).isEqualTo(1L);
		assertThat(reservedCaptor.getValue().getReservedQuantity()).isEqualTo(3);

		verify(producer, never()).publishFailed(any());
		verify(redisTemplate).delete("inventory:100");
	}

	@Test
	void reserveInventory_publishesFailedEventWhenStockIsInsufficient() {

		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.setIfAbsent(anyString(), eq("LOCKED"), any(Duration.class))).thenReturn(true);

		Inventory inventory = Inventory.builder().productId(200L).quantity(1).build();
		when(repository.findById(200L)).thenReturn(Optional.of(inventory));

		OrderCreatedEvent event = orderCreatedEvent(2L, 200L, 5);

		inventoryService.reserveInventory(event);

		verify(repository, never()).save(any());
		verify(producer, never()).publishReserved(any());

		ArgumentCaptor<InventoryFailedEvent> failedCaptor = ArgumentCaptor.forClass(InventoryFailedEvent.class);
		verify(producer).publishFailed(failedCaptor.capture());
		assertThat(failedCaptor.getValue().getOrderId()).isEqualTo(2L);
		assertThat(failedCaptor.getValue().getReason()).isEqualTo("Insufficient stock");
	}

	@Test
	void reserveInventory_doesNothingWhenTheRedisLockIsAlreadyHeld() {

		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.setIfAbsent(anyString(), eq("LOCKED"), any(Duration.class))).thenReturn(false);

		OrderCreatedEvent event = orderCreatedEvent(3L, 300L, 1);

		inventoryService.reserveInventory(event);

		verify(repository, never()).findById(any());
		verify(producer, never()).publishReserved(any());
		verify(producer, never()).publishFailed(any());
		verify(redisTemplate, never()).delete(anyString());
	}
}
