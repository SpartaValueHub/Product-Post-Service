package com.sparta.product_post_service.adaptor.in.kafka.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// reservation.events JSON (계약 외 필드는 무시)
@JsonIgnoreProperties(ignoreUnknown = true)
public record ReservationEventPayload(
		// CREATED | UPDATED | CANCELED
		String eventType,
		// 거래상태를 바꿀 판매글 UUID
		String productPostUuid
) {
}
