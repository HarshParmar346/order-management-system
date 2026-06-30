package com.harsh.order.service;

import com.harsh.order.dto.OrderRequest;
import com.harsh.order.dto.OrderResponse;

public interface OrderService {

	OrderResponse createOrder(OrderRequest request);

}