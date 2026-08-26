package com.sparta.product_post_service.adaptor.in.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.product_post_service.adaptor.in.kafka.vo.ReservationEventPayload;

class ReservationEventPayloadMapperTest {

	private final ReservationEventPayloadMapper mapper = new ReservationEventPayloadMapper(new ObjectMapper());

	@Test
	void parse_contractJson_readsEventTypeAndProductPostUuid() {
		String json = """
				{
				  "eventType": "CREATED",
				  "productPostUuid": "11111111-1111-1111-1111-111111111111",
				  "reservationUuid": "ignored",
				  "chatRoomUuid": "ignored"
				}
				""";

		ReservationEventPayload payload = mapper.parse(json).orElseThrow();

		assertThat(payload.eventType()).isEqualTo("CREATED");
		assertThat(payload.productPostUuid()).isEqualTo("11111111-1111-1111-1111-111111111111");
	}

	@Test
	void parse_invalidJson_isEmpty() {
		assertThat(mapper.parse("{")).isEmpty();
		assertThat(mapper.parse("")).isEmpty();
		assertThat(mapper.parse(null)).isEmpty();
	}
}
