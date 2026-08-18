package com.sparta.product_post_service.application.port.in.dto;

import com.sparta.product_post_service.domain.model.ProductPostStatus;

import lombok.Builder;
import lombok.Getter;

// 판매글 노출 상태 변경 Command
@Getter
@Builder
public class ChangeProductPostVisibilityCommand {

	// 변경할 노출 상태 (HIDDEN | PUBLIC)
	private final ProductPostStatus productPostStatus;
}
