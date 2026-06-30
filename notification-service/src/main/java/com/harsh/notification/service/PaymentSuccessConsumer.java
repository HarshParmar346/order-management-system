package com.harsh.notification.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.harsh.common.config.KafkaTopics;
import com.harsh.common.event.PaymentSuccessEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentSuccessConsumer {

	private final NotificationService service;

	@KafkaListener(topics = KafkaTopics.PAYMENT_SUCCESS, groupId = "notification-group")
	public void consume(PaymentSuccessEvent event) {

		service.sendSuccess(event);

	}

}
