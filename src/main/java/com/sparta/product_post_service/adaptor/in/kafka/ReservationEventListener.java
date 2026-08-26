package com.sparta.product_post_service.adaptor.in.kafka;

import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.sparta.product_post_service.adaptor.in.kafka.vo.ReservationEventPayload;
import com.sparta.product_post_service.application.port.in.ApplyReservationEventTradeStatusUseCase;
import com.sparta.product_post_service.application.service.ReservationEventTradeStatusResolver;
import com.sparta.product_post_service.domain.exception.ProductPostNotFoundException;
import com.sparta.product_post_service.domain.model.TradeStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// reservation.events inbound adapter (Controller와 같은 계층)
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "product-post.kafka", name = "enabled", havingValue = "true")
public class ReservationEventListener {

	// JSON → Payload
	private final ReservationEventPayloadMapper reservationEventPayloadMapper;
	// eventType → TradeStatus
	private final ReservationEventTradeStatusResolver reservationEventTradeStatusResolver;
	// 거래 상태 반영 UseCase
	private final ApplyReservationEventTradeStatusUseCase applyReservationEventTradeStatusUseCase;

	@KafkaListener(topics = "${product-post.kafka.reservation-events-topic}")
	public void consume(String payload) {
		Optional<ReservationEventPayload> parsed = reservationEventPayloadMapper.parse(payload);
		if (parsed.isEmpty()) {
			log.warn("예약 이벤트 JSON을 읽을 수 없어 skip 합니다.");
			return;
		}

		ReservationEventPayload event = parsed.get();
		Optional<TradeStatus> targetStatus = reservationEventTradeStatusResolver.resolve(event.eventType());
		if (targetStatus.isEmpty()) {
			log.debug("예약 이벤트 eventType={} 은 거래상태 변경 대상이 아니라 skip 합니다.", event.eventType());
			return;
		}

		if (event.productPostUuid() == null || event.productPostUuid().isBlank()) {
			log.warn("예약 이벤트에 productPostUuid가 없어 skip 합니다. eventType={}", event.eventType());
			return;
		}

		try {
			applyReservationEventTradeStatusUseCase.apply(event.productPostUuid(), targetStatus.get());
		} catch (ProductPostNotFoundException | IllegalArgumentException ex) {
			log.warn(
					"예약 이벤트 거래상태 반영을 skip 합니다. productPostUuid={}, eventType={}, reason={}",
					event.productPostUuid(),
					event.eventType(),
					ex.getMessage()
			);
		}
	}
}
