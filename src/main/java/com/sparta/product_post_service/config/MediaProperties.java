package com.sparta.product_post_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

// S3·CloudFront Presigned 업로드 설정 (컨테이너 env 주입)
@ConfigurationProperties(prefix = "app.media")
public record MediaProperties(
		// S3 버킷명
		String s3Bucket,
		// CloudFront 공개 base URL (끝 슬래시 없이)
		String cloudfrontBaseUrl,
		// AWS 리전
		String awsRegion,
		// 업로드 최대 바이트 (5MB)
		long maxBytes,
		// Presigned URL 유효 초
		int presignTtlSeconds
) {
}
