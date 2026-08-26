package com.sparta.product_post_service.application.service;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.product_post_service.application.port.in.ApplyReservationEventTradeStatusUseCase;
import com.sparta.product_post_service.application.port.out.ProductPostLoadPort;
import com.sparta.product_post_service.application.port.out.ProductPostSavePort;
import com.sparta.product_post_service.domain.exception.ProductPostNotFoundException;
import com.sparta.product_post_service.domain.model.ProductPost;
import com.sparta.product_post_service.domain.model.ProductPostStatus;
import com.sparta.product_post_service.domain.model.TradeStatus;

import lombok.RequiredArgsConstructor;

// 예약 이벤트 기반 거래 상태 변경 (소유권 검사 없음, 거래상태 컬럼만 갱신)
@Service
@RequiredArgsConstructor
public class ReservationEventTradeStatusService implements ApplyReservationEventTradeStatusUseCase {

	// 판매글 조회 Port
	private final ProductPostLoadPort productPostLoadPort;
	// 판매글 저장 Port
	private final ProductPostSavePort productPostSavePort;
	// 수정 시각용 시계
	private final Clock clock;

	@Override
	@Transactional
	public void apply(String productPostUuid, TradeStatus tradeStatus) {
		Objects.requireNonNull(tradeStatus, "거래 상태는 필수입니다.");

		if (productPostUuid == null || productPostUuid.isBlank()) {
			throw new ProductPostNotFoundException("판매글을 찾을 수 없습니다.");
		}

		ProductPost existing = productPostLoadPort.findByUuid(productPostUuid.trim())
				.filter(this::isMutable)
				.orElseThrow(() -> new ProductPostNotFoundException("판매글을 찾을 수 없습니다."));

		if (existing.getTradeStatus() == tradeStatus) {
			return;
		}

		existing.transitionTradeStatus(tradeStatus);
		existing.markUpdated(Instant.now(clock));
		productPostSavePort.updateTradeStatus(existing);
	}

	// 삭제되지 않은 글만 거래상태 변경 가능
	private boolean isMutable(ProductPost productPost) {
		return productPost.getDeletedAt() == null
				&& productPost.getProductPostStatus() != ProductPostStatus.DELETED;
	}
}
