package com.harsh.payment.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;

import com.harsh.common.event.InventoryReservedEvent;
import com.harsh.common.event.PaymentSuccessEvent;
import com.harsh.payment.entity.Payment;
import com.harsh.payment.enums.PaymentStatus;
import com.harsh.payment.repository.PaymentRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

	private final PaymentRepository repository;
	private final PaymentProducer producer;

	@Transactional
	@Override
	public void processPayment(InventoryReservedEvent event) {

		if (repository.findByOrderId(event.getOrderId()).isPresent()) {

			return;

		}

		boolean paymentSuccess = ThreadLocalRandom.current().nextInt(100) < 90;

		Payment payment = Payment.builder().orderId(event.getOrderId())
				.amount(event.getAmount().multiply(new BigDecimal(event.getReservedQuantity())))
				.createdAt(LocalDateTime.now()).build();

		if (paymentSuccess) {

			payment.setStatus(PaymentStatus.SUCCESS);

			payment.setTransactionId(UUID.randomUUID().toString());

			repository.save(payment);

			PaymentSuccessEvent successEvent = PaymentSuccessEvent.builder().metadata(event.getMetadata())
					.transactionId(payment.getTransactionId()).orderId(event.getOrderId()).build();

			producer.publishSuccess(successEvent);

		} else {

			payment.setStatus(PaymentStatus.FAILED);

			repository.save(payment);

			throw new RuntimeException("Gateway timeout");

		}

	}

}
