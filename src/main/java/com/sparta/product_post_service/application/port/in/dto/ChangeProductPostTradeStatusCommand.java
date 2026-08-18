package com.sparta.product_post_service.application.port.in.dto;

import com.sparta.product_post_service.domain.model.TradeStatus;

import lombok.Builder;
import lombok.Getter;

// 판매글 거래 상태 변경 Command
@Getter
@Builder
public class ChangeProductPostTradeStatusCommand {

	// 변경할 거래 상태
	private final TradeStatus tradeStatus;
}
