package com.harsh.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.harsh.common.dto.EventMetadata;
import com.harsh.common.event.PaymentFailedEvent;
import com.harsh.common.event.PaymentSuccessEvent;
import com.harsh.notification.entity.Notification;
import com.harsh.notification.repository.NotificationRepository;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

	@Mock
	private NotificationRepository repository;

	private NotificationServiceImpl notificationService;

	@BeforeEach
	void setUp() {
		notificationService = new NotificationServiceImpl(repository);
	}

	private EventMetadata metadata() {
		return EventMetadata.builder().eventId(UUID.randomUUID()).createdAt(LocalDateTime.now()).version(1).build();
	}

	@Test
	void sendSuccess_persistsANotificationTaggedAsSuccess() {

		PaymentSuccessEvent event = PaymentSuccessEvent.builder().metadata(metadata()).orderId(1L)
				.transactionId("txn-123").build();

		notificationService.sendSuccess(event);

		ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
		verify(repository).save(captor.capture());

		Notification saved = captor.getValue();
		assertThat(saved.getOrderId()).isEqualTo(1L);
		assertThat(saved.getType()).isEqualTo("SUCCESS");
		assertThat(saved.getMessage()).isEqualTo("Payment Successful");
	}

	@Test
	void sendFailure_persistsANotificationTaggedAsFailure() {

		PaymentFailedEvent event = PaymentFailedEvent.builder().metadata(metadata()).orderId(2L)
				.reason("Gateway timeout").build();

		notificationService.sendFailure(event);

		ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
		verify(repository).save(captor.capture());

		Notification saved = captor.getValue();
		assertThat(saved.getOrderId()).isEqualTo(2L);
		assertThat(saved.getType()).isEqualTo("SUCCESS");
		assertThat(saved.getMessage()).isEqualTo("Payment Failed");
	}
}
