package com.harsh.notification.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.harsh.common.config.KafkaTopics;
import com.harsh.common.event.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentFailureConsumer {

	private final NotificationService service;

	@KafkaListener(topics = KafkaTopics.PAYMENT_FAILED, groupId = "notification-group")
	public void consume(PaymentFailedEvent event) {

		service.sendFailure(event);

	}

}