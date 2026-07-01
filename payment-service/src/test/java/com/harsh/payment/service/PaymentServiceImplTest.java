package com.harsh.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.harsh.common.dto.EventMetadata;
import com.harsh.common.event.InventoryReservedEvent;
import com.harsh.common.event.PaymentSuccessEvent;
import com.harsh.payment.entity.Payment;
import com.harsh.payment.enums.PaymentStatus;
import com.harsh.payment.repository.PaymentRepository;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

	@Mock
	private PaymentRepository repository;

	@Mock
	private PaymentProducer producer;

	private PaymentServiceImpl paymentService;

	@BeforeEach
	void setUp() {
		paymentService = new PaymentServiceImpl(repository, producer);
	}

	private InventoryReservedEvent reservedEvent(long orderId) {

		return InventoryReservedEvent.builder()
				.metadata(EventMetadata.builder().eventId(UUID.randomUUID()).createdAt(LocalDateTime.now()).version(1)
						.build())
				.orderId(orderId).productId(1L).reservedQuantity(2).amount(new BigDecimal("25.00")).build();
	}

	@Test
	void processPayment_isIdempotentWhenAPaymentAlreadyExistsForTheOrder() {

		when(repository.findByOrderId(1L)).thenReturn(Optional.of(new Payment()));

		paymentService.processPayment(reservedEvent(1L));

		verify(repository, never()).save(any());
		verify(producer, never()).publishSuccess(any());
	}

	@Test
	void processPayment_onSuccess_savesPaymentAndPublishesSuccessEvent() {

		when(repository.findByOrderId(2L)).thenReturn(Optional.empty());

		try (MockedStatic<ThreadLocalRandom> mockedRandom = Mockito.mockStatic(ThreadLocalRandom.class)) {

			ThreadLocalRandom random = Mockito.mock(ThreadLocalRandom.class);
			mockedRandom.when(ThreadLocalRandom::current).thenReturn(random);
			when(random.nextInt(100)).thenReturn(0); // < 90 -> success

			paymentService.processPayment(reservedEvent(2L));
		}

		ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
		verify(repository).save(paymentCaptor.capture());

		Payment savedPayment = paymentCaptor.getValue();
		assertThat(savedPayment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
		assertThat(savedPayment.getOrderId()).isEqualTo(2L);
		assertThat(savedPayment.getAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
		assertThat(savedPayment.getTransactionId()).isNotBlank();

		ArgumentCaptor<PaymentSuccessEvent> eventCaptor = ArgumentCaptor.forClass(PaymentSuccessEvent.class);
		verify(producer).publishSuccess(eventCaptor.capture());
		assertThat(eventCaptor.getValue().getOrderId()).isEqualTo(2L);
	}

	@Test
	void processPayment_onFailure_savesFailedPaymentAndThrowsWithoutPublishingSuccess() {

		when(repository.findByOrderId(3L)).thenReturn(Optional.empty());

		try (MockedStatic<ThreadLocalRandom> mockedRandom = Mockito.mockStatic(ThreadLocalRandom.class)) {

			ThreadLocalRandom random = Mockito.mock(ThreadLocalRandom.class);
			mockedRandom.when(ThreadLocalRandom::current).thenReturn(random);
			when(random.nextInt(100)).thenReturn(99); // >= 90 -> failure

			assertThatThrownBy(() -> paymentService.processPayment(reservedEvent(3L)))
					.isInstanceOf(RuntimeException.class).hasMessage("Gateway timeout");
		}

		ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
		verify(repository).save(paymentCaptor.capture());
		assertThat(paymentCaptor.getValue().getStatus()).isEqualTo(PaymentStatus.FAILED);

		verify(producer, never()).publishSuccess(any());
	}
}
