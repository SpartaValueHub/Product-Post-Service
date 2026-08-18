package com.sparta.product_post_service.adaptor.in.web.vo;

import com.sparta.product_post_service.domain.model.ProductPostStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 판매글 노출 상태 변경 HTTP 요청 VO
@Getter
@NoArgsConstructor
public class ChangeProductPostVisibilityRequestVo {

	// 변경할 노출 상태 (HIDDEN | PUBLIC)
	@NotNull(message = "노출 상태는 필수입니다.")
	private ProductPostStatus productPostStatus;
}
