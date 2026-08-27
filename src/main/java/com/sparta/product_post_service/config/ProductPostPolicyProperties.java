package com.sparta.product_post_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

// 판매글 서비스 정책 (application.yml product-post.policy.*)
@ConfigurationProperties(prefix = "product-post.policy")
public record ProductPostPolicyProperties(
		// 판매 최소 가격 (원)
		long minPrice,
		// 일반회원 하루 끌올 한도 (회)
		int bumpDailyLimit,
		// 동일 상품 끌올 쿨다운 (시간)
		long bumpCooldownHours,
		// 목록·검색 반경 (km, 거래희망장소 기준)
		double searchRadiusKm
) {
}
