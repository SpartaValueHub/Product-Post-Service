package com.sparta.listing_service.adaptor.in.web.vo;

import java.math.BigDecimal;
import java.util.List;

import com.sparta.listing_service.adaptor.in.web.validation.MinListingPrice;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 판매글 등록 HTTP 요청 VO
@Getter
@NoArgsConstructor
public class CreateListingRequestVo {

	// 리프 카테고리 UUID
	@NotBlank(message = "카테고리 UUID는 필수입니다.")
	private String categoryUuid;

	// 상품명
	@NotBlank(message = "상품명은 필수입니다.")
	@Size(min = 2, max = 100, message = "상품명은 2~100자여야 합니다.")
	private String listingName;

	// 상품 상태 등급
	@NotBlank(message = "상품 상태 등급은 필수입니다.")
	private String conditionGrade;

	// 가격 (최소가는 listing.policy.min-price)
	@NotNull(message = "가격은 필수입니다.")
	@MinListingPrice
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

	// 상품 이미지 목록 (1~10)
	@NotEmpty(message = "상품 사진은 최소 1장 필요합니다.")
	@Size(max = 10, message = "상품 사진은 최대 10장까지 가능합니다.")
	@Valid
	private List<CreateListingImageRequestVo> images;

	// 서류 목록 (선택)
	@Valid
	private List<CreateListingDocumentRequestVo> documents;
}
