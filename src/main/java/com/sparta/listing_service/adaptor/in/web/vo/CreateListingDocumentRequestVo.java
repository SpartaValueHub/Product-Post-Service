package com.sparta.listing_service.adaptor.in.web.vo;

import com.sparta.listing_service.domain.model.DocumentType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 판매글 등록 요청 - 서류 VO
@Getter
@NoArgsConstructor
public class CreateListingDocumentRequestVo {

	// 서류 종류 (WARRANTY | RECEIPT | APPRAISAL)
	@NotNull(message = "서류 종류는 필수입니다.")
	private DocumentType documentType;

	// 서류 이미지 URL
	@NotBlank(message = "서류 이미지 경로는 필수입니다.")
	@Size(max = 500, message = "서류 이미지 경로는 최대 500자까지 가능합니다.")
	private String imageUrl;
}
