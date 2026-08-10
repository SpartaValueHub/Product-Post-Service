package com.sparta.listing_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

// 판매글 서비스 정책 (application.yml listing.policy.*)
@ConfigurationProperties(prefix = "listing.policy")
public record ListingPolicyProperties(
		// 판매 최소 가격 (원)
		long minPrice
) {
}
