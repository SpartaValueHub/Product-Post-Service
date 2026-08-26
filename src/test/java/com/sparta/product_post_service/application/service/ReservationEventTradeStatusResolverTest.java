package com.sparta.product_post_service.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.sparta.product_post_service.config.ProductPostKafkaProperties;
import com.sparta.product_post_service.domain.model.TradeStatus;

class ReservationEventTradeStatusResolverTest {

	@Test
	void resolve_mappedCreated_returnsReserved() {
		ReservationEventTradeStatusResolver resolver = resolverWith(Map.of("CREATED", TradeStatus.RESERVED));

		assertThat(resolver.resolve("CREATED")).contains(TradeStatus.RESERVED);
		assertThat(resolver.resolve("created")).contains(TradeStatus.RESERVED);
	}

	@Test
	void resolve_unmappedUpdated_isEmpty() {
		ReservationEventTradeStatusResolver resolver = resolverWith(Map.of("CREATED", TradeStatus.RESERVED));

		assertThat(resolver.resolve("UPDATED")).isEmpty();
		assertThat(resolver.resolve("CANCELED")).isEmpty();
		assertThat(resolver.resolve("")).isEmpty();
		assertThat(resolver.resolve(null)).isEmpty();
	}

	private ReservationEventTradeStatusResolver resolverWith(Map<String, TradeStatus> mapping) {
		return new ReservationEventTradeStatusResolver(
				new ProductPostKafkaProperties(true, "reservation.events", mapping)
		);
	}
}
