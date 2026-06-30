package com.harsh.order.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harsh.common.event.OrderCreatedEvent;
import com.harsh.order.entity.OutboxEvent;
import com.harsh.order.repository.OutboxEventRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class OutboxServiceImpl implements OutboxService {

	private final OutboxEventRepository repository;

	private final OrderEventProducer producer;

	private final ObjectMapper mapper;

	@Override
	public void publishPendingEvents() {

		List<OutboxEvent> events = repository.findByPublishedFalse();

		for (OutboxEvent outbox : events) {

			try {

				OrderCreatedEvent event = mapper.readValue(outbox.getPayload(), OrderCreatedEvent.class);

				producer.publish(event);

				outbox.setPublished(true);

				repository.save(outbox);

			} catch (Exception ex) {

				ex.printStackTrace();

			}

		}

	}

}