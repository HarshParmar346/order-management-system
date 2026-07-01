package com.harsh.order.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.harsh.order.dto.OrderRequest;
import com.harsh.order.entity.Order;
import com.harsh.order.enums.OrderStatus;

class OrderMapperTest {

	private final OrderMapper mapper = new OrderMapper();

	@Test
	void toEntity_mapsAllFieldsFromRequest() {

		OrderRequest request = new OrderRequest(101L, 202L, 3);

		Order order = mapper.toEntity(request);

		assertThat(order.getCustomerId()).isEqualTo(101L);
		assertThat(order.getProductId()).isEqualTo(202L);
		assertThat(order.getQuantity()).isEqualTo(3);
	}

	@Test
	void toEntity_setsInitialStatusToCreated() {

		OrderRequest request = new OrderRequest(1L, 2L, 1);

		Order order = mapper.toEntity(request);

		assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
	}

	@Test
	void toEntity_setsCreatedAtTimestamp() {

		OrderRequest request = new OrderRequest(1L, 2L, 1);

		Order order = mapper.toEntity(request);

		assertThat(order.getCreatedAt()).isNotNull();
	}

	@Test
	void toEntity_doesNotSetIdOrAmount() {

		OrderRequest request = new OrderRequest(1L, 2L, 5);

		Order order = mapper.toEntity(request);

		assertThat(order.getId()).isNull();
		assertThat(order.getAmount()).isNull();
	}
}
