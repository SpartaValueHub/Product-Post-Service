package com.sparta.product_post_service.config;

import java.util.List;
import java.util.Map;

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
		int presignTtlSeconds,
		// 미확정 객체 prefix
		String pendingPrefix,
		// 확정 객체 prefix
		String confirmedPrefix,
		// Content-Type → 확장자
		Map<String, String> extensionByContentType,
		// 승격 없이 그대로 저장할 URL (판매글은 비움)
		List<String> passthroughUrls
) {

	public MediaProperties {
		if (pendingPrefix == null || pendingPrefix.isBlank()) {
			pendingPrefix = "pending/";
		}
		if (confirmedPrefix == null || confirmedPrefix.isBlank()) {
			confirmedPrefix = "posts/";
		}
		if (extensionByContentType == null || extensionByContentType.isEmpty()) {
			extensionByContentType = Map.of(
					"image/jpeg", "jpg",
					"image/png", "png",
					"image/webp", "webp",
					"image/gif", "gif"
			);
		} else {
			extensionByContentType = Map.copyOf(extensionByContentType);
		}
		if (passthroughUrls == null) {
			passthroughUrls = List.of();
		} else {
			passthroughUrls = List.copyOf(passthroughUrls);
		}
	}
}
