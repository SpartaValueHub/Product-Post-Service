package com.sparta.product_post_service.application.port.in;

import com.sparta.product_post_service.application.port.in.dto.ChangeProductPostTradeStatusCommand;
import com.sparta.product_post_service.application.port.in.dto.ProductPostSummaryDto;

// 판매글 거래 상태 변경 Input Port
public interface ChangeProductPostTradeStatusUseCase {

	// 거래 상태 전이 (판매자 본인, Domain 전이 규칙 적용)
	ProductPostSummaryDto changeTradeStatus(
			String memberUuid,
			String productPostUuid,
			ChangeProductPostTradeStatusCommand command
	);
}
