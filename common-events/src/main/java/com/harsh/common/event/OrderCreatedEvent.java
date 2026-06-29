package com.harsh.common.event;

import java.math.BigDecimal;

import com.harsh.common.dto.EventMetadata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {

    private EventMetadata metadata;

    private Long orderId;

    private Long customerId;

    private Long productId;

    private Integer quantity;

    private BigDecimal amount;

}