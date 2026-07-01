package com.harsh.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harsh.common.dto.EventMetadata;
import com.harsh.common.event.OrderCreatedEvent;
import com.harsh.order.entity.OutboxEvent;
import com.harsh.order.repository.OutboxEventRepository;

@ExtendWith(MockitoExtension.class)
class OutboxServiceImplTest {

	@Mock
	private OutboxEventRepository repository;

	@Mock
	private OrderEventProducer producer;

	private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

	private OutboxServiceImpl outboxService;

	@BeforeEach
	void setUp() {
		outboxService = new OutboxServiceImpl(repository, producer, mapper);
	}

	private String serializedEvent(long orderId) throws Exception {

		OrderCreatedEvent event = OrderCreatedEvent.builder()
				.metadata(EventMetadata.builder().eventId(UUID.randomUUID()).createdAt(LocalDateTime.now()).version(1)
						.build())
				.orderId(orderId).customerId(1L).productId(2L).quantity(1).amount(new java.math.BigDecimal(100))
				.build();

		return mapper.writeValueAsString(event);
	}

	@Test
	void publishPendingEvents_publishesEachUnpublishedEventAndMarksItPublished() throws Exception {

		OutboxEvent pending = OutboxEvent.builder().id(1L).eventType("order-created").payload(serializedEvent(10L))
				.published(false).build();

		when(repository.findByPublishedFalse()).thenReturn(List.of(pending));

		outboxService.publishPendingEvents();

		verify(producer, times(1)).publish(any(OrderCreatedEvent.class));

		ArgumentCaptor<OutboxEvent> savedCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
		verify(repository).save(savedCaptor.capture());
		assertThat(savedCaptor.getValue().isPublished()).isTrue();
	}

	@Test
	void publishPendingEvents_doesNothingWhenThereAreNoPendingEvents() {

		when(repository.findByPublishedFalse()).thenReturn(List.of());

		outboxService.publishPendingEvents();

		verify(producer, never()).publish(any());
		verify(repository, never()).save(any());
	}

	@Test
	void publishPendingEvents_skipsAnEventWithCorruptedPayloadWithoutThrowing() throws Exception {

		OutboxEvent corrupted = OutboxEvent.builder().id(2L).eventType("order-created").payload("not-valid-json")
				.published(false).build();
		OutboxEvent healthy = OutboxEvent.builder().id(3L).eventType("order-created").payload(serializedEvent(11L))
				.published(false).build();

		when(repository.findByPublishedFalse()).thenReturn(List.of(corrupted, healthy));

		outboxService.publishPendingEvents();

		// the corrupted event is logged and skipped; the healthy one still gets published
		verify(producer, times(1)).publish(any(OrderCreatedEvent.class));
		verify(repository, times(1)).save(any(OutboxEvent.class));
	}
}
