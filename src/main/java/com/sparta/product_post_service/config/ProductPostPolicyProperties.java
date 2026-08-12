package com.sparta.product_post_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

// 판매글 서비스 정책 (application.yml product-post.policy.*)
@ConfigurationProperties(prefix = "product-post.policy")
public record ProductPostPolicyProperties(
		// 판매 최소 가격 (원)
		long minPrice
) {
}
