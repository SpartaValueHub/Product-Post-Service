package com.sparta.product_post_service.config;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.sparta.product_post_service.domain.model.TradeStatus;

// 예약 이벤트 Kafka 구독 설정 (application.yml product-post.kafka.*)
@ConfigurationProperties(prefix = "product-post.kafka")
public record ProductPostKafkaProperties(
		// 리스너 기동 여부 (local 기본 false)
		boolean enabled,
		// 예약 도메인 이벤트 토픽
		String reservationEventsTopic,
		// eventType → 적용할 거래 상태 (미등록 타입은 무시)
		Map<String, TradeStatus> eventTypeTradeStatus
) {
}
