package com.harsh.inventory.service;

import com.harsh.common.event.OrderCreatedEvent;

public interface InventoryService {

	void reserveInventory(OrderCreatedEvent event);

}