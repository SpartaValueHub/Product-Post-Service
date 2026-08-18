package com.sparta.product_post_service.adaptor.in.web.vo;

import com.sparta.product_post_service.domain.model.TradeStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 판매글 거래 상태 변경 HTTP 요청 VO
@Getter
@NoArgsConstructor
public class ChangeProductPostTradeStatusRequestVo {

	// 변경할 거래 상태 (SELLING | RESERVED | SOLD_OUT)
	@NotNull(message = "거래 상태는 필수입니다.")
	private TradeStatus tradeStatus;
}
