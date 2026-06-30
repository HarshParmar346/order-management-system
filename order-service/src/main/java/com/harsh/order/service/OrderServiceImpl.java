package com.harsh.order.service;

import com.harsh.common.config.KafkaTopics;
import com.harsh.common.dto.EventMetadata;
import com.harsh.common.event.OrderCreatedEvent;
import com.harsh.order.dto.OrderRequest;
import com.harsh.order.dto.OrderResponse;
import com.harsh.order.entity.Order;
import com.harsh.order.entity.OutboxEvent;
import com.harsh.order.enums.OrderStatus;
import com.harsh.order.mapper.OrderMapper;
import com.harsh.order.repository.OrderRepository;
import com.harsh.order.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

	private final OrderRepository orderRepository;

	private final OutboxEventRepository outboxRepository;

	private final OrderMapper orderMapper;

	private final ObjectMapper objectMapper;

	@Override
	public OrderResponse createOrder(OrderRequest request) {

		Order order = orderMapper.toEntity(request);

		// Dummy pricing for now
		order.setAmount(new BigDecimal(request.getQuantity() * 100.00));

		order.setStatus(OrderStatus.CREATED);

		Order savedOrder = orderRepository.save(order);

		OrderCreatedEvent event = OrderCreatedEvent.builder()
				.metadata(EventMetadata.builder().eventId(UUID.randomUUID()).createdAt(LocalDateTime.now()).version(1)
						.build())
				.orderId(savedOrder.getId()).customerId(savedOrder.getCustomerId()).productId(savedOrder.getProductId())
				.quantity(savedOrder.getQuantity()).amount(savedOrder.getAmount()).build();

		saveOutboxEvent(event);

		return OrderResponse.builder().orderId(savedOrder.getId()).status(savedOrder.getStatus().name()).build();
	}

	private void saveOutboxEvent(OrderCreatedEvent event) {

		try {

			String payload = objectMapper.writeValueAsString(event);

			OutboxEvent outbox = OutboxEvent.builder().eventType(KafkaTopics.ORDER_CREATED).payload(payload)
					.published(false).createdAt(LocalDateTime.now()).build();

			outboxRepository.save(outbox);

		} catch (JsonProcessingException e) {

			throw new RuntimeException("Failed to serialize event", e);

		}

	}

}