package com.sparta.product_post_service.adaptor.in.web.vo;

import java.math.BigDecimal;
import java.util.List;

import com.sparta.product_post_service.adaptor.in.web.validation.MinProductPostPrice;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 판매글 수정 HTTP 요청 VO (등록과 동일 필드, 전체 교체)
@Getter
@NoArgsConstructor
public class UpdateProductPostRequestVo {

	// 리프 카테고리 UUID
	@NotBlank(message = "카테고리 UUID는 필수입니다.")
	private String categoryUuid;

	// 상품명
	@NotBlank(message = "상품명은 필수입니다.")
	@Size(min = 2, max = 100, message = "상품명은 2~100자여야 합니다.")
	private String productPostName;

	// 상품 상태 등급
	@NotBlank(message = "상품 상태 등급은 필수입니다.")
	private String conditionGrade;

	// 가격 (최소가는 product-post.policy.min-price)
	@NotNull(message = "가격은 필수입니다.")
	@MinProductPostPrice
	private Long price;

	// 상세 설명
	@NotBlank(message = "상세 설명은 필수입니다.")
	@Size(max = 2000, message = "상세 설명은 최대 2000자까지 가능합니다.")
	private String description;

	// 위도
	@NotNull(message = "위도는 필수입니다.")
	private BigDecimal latitude;

	// 경도
	@NotNull(message = "경도는 필수입니다.")
	private BigDecimal longitude;

	// 거래 장소명
	@NotBlank(message = "거래 장소명은 필수입니다.")
	@Size(max = 100, message = "거래 장소명은 최대 100자까지 가능합니다.")
	private String placeName;

	// 거래 희망 동(읍면동, 선택)
	@Size(max = 50, message = "거래 희망 동은 최대 50자까지 가능합니다.")
	private String regionDong;

	// 거래 희망 구(시군구, 선택)
	@Size(max = 50, message = "거래 희망 구는 최대 50자까지 가능합니다.")
	private String regionGu;

	// 상품 이미지 목록 (1~10, 전체 교체)
	@NotEmpty(message = "상품 사진은 최소 1장 필요합니다.")
	@Size(max = 10, message = "상품 사진은 최대 10장까지 가능합니다.")
	@Valid
	private List<CreateProductPostImageRequestVo> images;

	// 서류 목록 (선택, 전체 교체)
	@Valid
	private List<CreateProductPostDocumentRequestVo> documents;
}
