package com.harsh.order.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OutboxScheduler {

	private final OutboxService outboxService;

	@Scheduled(fixedDelay = 5000)
	public void publish() {

		outboxService.publishPendingEvents();

	}

}