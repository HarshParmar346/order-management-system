package com.harsh.payment.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.harsh.common.config.KafkaTopics;
import com.harsh.common.event.PaymentFailedEvent;
import com.harsh.common.event.PaymentSuccessEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentProducer {

	private final KafkaTemplate<String, Object> kafkaTemplate;

	public void publishSuccess(PaymentSuccessEvent event) {

		kafkaTemplate.send(KafkaTopics.PAYMENT_SUCCESS, event.getOrderId().toString(), event);

	}

	public void publishFailed(PaymentFailedEvent event) {

		kafkaTemplate.send(KafkaTopics.PAYMENT_FAILED, event.getOrderId().toString(), event);

	}

}