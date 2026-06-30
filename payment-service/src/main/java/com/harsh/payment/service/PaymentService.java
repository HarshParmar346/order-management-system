package com.harsh.payment.service;

import com.harsh.common.event.InventoryReservedEvent;

public interface PaymentService {

	void processPayment(InventoryReservedEvent event);

}