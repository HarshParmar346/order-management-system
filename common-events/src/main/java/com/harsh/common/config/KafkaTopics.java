package com.harsh.common.config;

public final class KafkaTopics {

	private KafkaTopics() {
	}

	public static final String ORDER_CREATED = "order-created";

	public static final String INVENTORY_RESERVED = "inventory-reserved";
	public static final String INVENTORY_FAILED = "inventory-failed";

	public static final String PAYMENT_EVENTS = "payment-events";

	public static final String NOTIFICATION_EVENTS = "notification-events";

	public static final String PAYMENT_DLT = "payment-dlt";

	public static final String INVENTORY_DLT = "inventory-dlt";

	public static final String PAYMENT_SUCCESS = "payment-success";

	public static final String PAYMENT_FAILED = "payment-failed";

}
