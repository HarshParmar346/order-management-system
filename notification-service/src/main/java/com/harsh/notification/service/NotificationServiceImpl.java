package com.harsh.notification.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.harsh.common.event.PaymentFailedEvent;
import com.harsh.common.event.PaymentSuccessEvent;
import com.harsh.notification.entity.Notification;
import com.harsh.notification.repository.NotificationRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

	private final NotificationRepository repository;

	@Override
	public void sendSuccess(PaymentSuccessEvent event) {

		Notification notification = Notification.builder().orderId(event.getOrderId()).type("SUCCESS")
				.message("Payment Successful").createdAt(LocalDateTime.now()).build();

		repository.save(notification);

	}

	@Override
	public void sendFailure(PaymentFailedEvent event) {

		Notification notification = Notification.builder().orderId(event.getOrderId()).type("SUCCESS")
				.message("Payment Failed").createdAt(LocalDateTime.now()).build();

		repository.save(notification);

	}

}