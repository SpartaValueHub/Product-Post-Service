package com.sparta.product_post_service.application.port.out;

import java.util.Optional;

import com.sparta.product_post_service.domain.model.ProductPost;

// 판매글 조회 Output Port
public interface ProductPostLoadPort {

	// 판매글 UUID로 단건 조회 (이미지·서류 포함)
	Optional<ProductPost> findByUuid(String productPostUuid);

	// 판매글 ID로 단건 조회 (이미지·서류 포함)
	Optional<ProductPost> findById(Long productPostId);
}
