package com.sparta.product_post_service.application.service;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.sparta.product_post_service.config.ProductPostKafkaProperties;
import com.sparta.product_post_service.domain.model.TradeStatus;

// 예약 eventType → TradeStatus (설정 맵만 사용, 코드에 타입 하드코딩 없음)
@Component
public class ReservationEventTradeStatusResolver {

	// 대문자 정규화된 eventType → 거래 상태
	private final Map<String, TradeStatus> eventTypeToTradeStatus;

	public ReservationEventTradeStatusResolver(ProductPostKafkaProperties kafkaProperties) {
		Map<String, TradeStatus> source = kafkaProperties.eventTypeTradeStatus();
		Map<String, TradeStatus> normalized = new LinkedHashMap<>();
		if (source != null) {
			source.forEach((eventType, tradeStatus) -> {
				if (eventType != null && !eventType.isBlank() && tradeStatus != null) {
					normalized.put(eventType.trim().toUpperCase(Locale.ROOT), tradeStatus);
				}
			});
		}
		this.eventTypeToTradeStatus = Map.copyOf(normalized);
	}

	// 매핑 없으면 empty (UPDATED 등 무시)
	public Optional<TradeStatus> resolve(String eventType) {
		if (eventType == null || eventType.isBlank()) {
			return Optional.empty();
		}
		return Optional.ofNullable(eventTypeToTradeStatus.get(eventType.trim().toUpperCase(Locale.ROOT)));
	}
}
