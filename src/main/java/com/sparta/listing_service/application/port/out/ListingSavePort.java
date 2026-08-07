package com.sparta.listing_service.application.port.out;

import com.sparta.listing_service.domain.model.Listing;

// 판매글 저장 Output Port
public interface ListingSavePort {

	// 판매글 신규 저장 (이미지·서류 포함, 저장 후 ID가 채워진 Domain 반환)
	Listing save(Listing listing);

	// 기존 판매글 변경 저장 (이미지·서류 포함)
	Listing update(Listing listing);
}
