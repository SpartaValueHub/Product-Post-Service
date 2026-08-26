package com.sparta.product_post_service.application.port.out;

import com.sparta.product_post_service.domain.model.ProductPost;

// 판매글 저장 Output Port
public interface ProductPostSavePort {

	// 판매글 신규 저장 (이미지·서류 포함, 저장 후 ID가 채워진 Domain 반환)
	ProductPost save(ProductPost listing);

	// 기존 판매글 변경 저장 (이미지·서류 포함)
	ProductPost update(ProductPost listing);

	// 거래 상태·수정시각만 갱신 (이미지·서류 미터치)
	void updateTradeStatus(ProductPost listing);
}
