package com.sparta.listing_service.adaptor.in.web.vo;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 판매글 등록 요청 - 이미지 VO
@Getter
@NoArgsConstructor
public class CreateProductPostImageRequestVo {

	// 이미지 URL
	@NotBlank(message = "이미지 경로는 필수입니다.")
	@Size(max = 500, message = "이미지 경로는 최대 500자까지 가능합니다.")
	private String imageUrl;

	// 노출 순서
	@NotNull(message = "이미지 노출 순서는 필수입니다.")
	@Min(value = 1, message = "이미지 노출 순서는 1 이상이어야 합니다.")
	private Integer sortOrder;
}
