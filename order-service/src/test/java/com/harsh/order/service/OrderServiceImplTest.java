package com.harsh.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harsh.order.dto.OrderRequest;
import com.harsh.order.dto.OrderResponse;
import com.harsh.order.entity.Order;
import com.harsh.order.entity.OutboxEvent;
import com.harsh.order.enums.OrderStatus;
import com.harsh.order.mapper.OrderMapper;
import com.harsh.order.repository.OrderRepository;
import com.harsh.order.repository.OutboxEventRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private OutboxEventRepository outboxRepository;

	private final OrderMapper orderMapper = new OrderMapper();

	private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

	private OrderServiceImpl orderService;

	@BeforeEach
	void setUp() {
		orderService = new OrderServiceImpl(orderRepository, outboxRepository, orderMapper, objectMapper);
	}

	@Test
	void createOrder_persistsOrderWithCreatedStatusAndCalculatedAmount() {

		OrderRequest request = new OrderRequest(1L, 2L, 4);

		when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
			Order order = invocation.getArgument(0);
			order.setId(99L);
			return order;
		});

		OrderResponse response = orderService.createOrder(request);

		ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
		verify(orderRepository).save(orderCaptor.capture());

		Order savedOrder = orderCaptor.getValue();
		assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.CREATED);
		assertThat(savedOrder.getAmount()).isEqualByComparingTo(new BigDecimal(4 * 100.00));

		assertThat(response.getOrderId()).isEqualTo(99L);
		assertThat(response.getStatus()).isEqualTo("CREATED");
	}

	@Test
	void createOrder_writesAnUnpublishedOutboxEventContainingTheOrderCreatedPayload() {

		OrderRequest request = new OrderRequest(5L, 6L, 2);

		when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
			Order order = invocation.getArgument(0);
			order.setId(42L);
			return order;
		});

		orderService.createOrder(request);

		ArgumentCaptor<OutboxEvent> outboxCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
		verify(outboxRepository).save(outboxCaptor.capture());

		OutboxEvent savedEvent = outboxCaptor.getValue();
		assertThat(savedEvent.isPublished()).isFalse();
		assertThat(savedEvent.getEventType()).isEqualTo("order-created");
		assertThat(savedEvent.getPayload()).contains("\"orderId\":42");
		assertThat(savedEvent.getPayload()).contains("\"customerId\":5");
	}
}
