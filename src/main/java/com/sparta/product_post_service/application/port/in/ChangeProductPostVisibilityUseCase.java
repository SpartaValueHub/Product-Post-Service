package com.sparta.product_post_service.application.port.in;

import com.sparta.product_post_service.application.port.in.dto.ChangeProductPostVisibilityCommand;
import com.sparta.product_post_service.application.port.in.dto.ProductPostSummaryDto;

// 판매글 노출 상태 변경 Input Port (숨김·재공개)
public interface ChangeProductPostVisibilityUseCase {

	// 노출 상태 변경 (판매자 본인, HIDDEN|PUBLIC만)
	ProductPostSummaryDto changeVisibility(
			String memberUuid,
			String productPostUuid,
			ChangeProductPostVisibilityCommand command
	);
}
