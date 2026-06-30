package com.harsh.order.service;

public interface OutboxService {

	void publishPendingEvents();

}