package com.harsh.order.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class OrderResponse {

    private Long orderId;

    private String status;

}