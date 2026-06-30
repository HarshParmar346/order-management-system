package com.harsh.order.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.harsh.order.dto.OrderRequest;
import com.harsh.order.dto.OrderResponse;
import com.harsh.order.service.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

	private final OrderService service;

	@PostMapping
	public ResponseEntity<OrderResponse> create(

			@Valid @RequestBody OrderRequest request) {

		return ResponseEntity.ok(service.createOrder(request));
	}

}