package com.harsh.notification.service;

import com.harsh.common.event.PaymentFailedEvent;
import com.harsh.common.event.PaymentSuccessEvent;

public interface NotificationService {

	void sendSuccess(PaymentSuccessEvent event);

	void sendFailure(PaymentFailedEvent event);

}