package com.sparta.product_post_service.application.port.in;

import com.sparta.product_post_service.domain.model.TradeStatus;

// 예약 이벤트로 판매글 거래 상태를 변경하는 Input Port (판매자 인증 없음)
public interface ApplyReservationEventTradeStatusUseCase {

	// productPostUuid 글에 tradeStatus 전이 (Domain 규칙, 멱등)
	void apply(String productPostUuid, TradeStatus tradeStatus);
}
