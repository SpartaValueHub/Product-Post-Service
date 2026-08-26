package com.sparta.product_post_service.adaptor.in.kafka;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.product_post_service.adaptor.in.kafka.vo.ReservationEventPayload;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 예약 이벤트 JSON → Payload (파싱 실패는 empty)
@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationEventPayloadMapper {

	// JSON 변환기 (Spring 빈, static 사용 안 함)
	private final ObjectMapper objectMapper;

	// 잘못된 JSON이면 empty
	public Optional<ReservationEventPayload> parse(String payload) {
		if (payload == null || payload.isBlank()) {
			return Optional.empty();
		}
		try {
			return Optional.ofNullable(objectMapper.readValue(payload, ReservationEventPayload.class));
		} catch (JsonProcessingException ex) {
			log.warn("예약 이벤트 JSON 파싱 실패: {}", ex.getOriginalMessage());
			return Optional.empty();
		}
	}
}
