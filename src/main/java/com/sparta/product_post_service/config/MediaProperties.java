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

	// yaml 미바인딩·empty map 대비 기본 허용 Content-Type
	public static final Map<String, String> DEFAULT_EXTENSION_BY_CONTENT_TYPE = Map.of(
			"image/jpeg", "jpg",
			"image/png", "png",
			"image/webp", "webp",
			"image/gif", "gif"
	);

	public MediaProperties {
		if (pendingPrefix == null || pendingPrefix.isBlank()) {
			pendingPrefix = "pending/";
		}
		if (confirmedPrefix == null || confirmedPrefix.isBlank()) {
			confirmedPrefix = "posts/";
		}
		if (extensionByContentType == null || extensionByContentType.isEmpty()) {
			extensionByContentType = DEFAULT_EXTENSION_BY_CONTENT_TYPE;
		} else {
			extensionByContentType = Map.copyOf(extensionByContentType);
		}
		if (passthroughUrls == null) {
			passthroughUrls = List.of();
		} else {
			passthroughUrls = List.copyOf(passthroughUrls);
		}
	}

	// empty map이면 jpeg/png/webp/gif 기본값을 쓴다
	public Map<String, String> resolvedExtensionByContentType() {
		if (extensionByContentType == null || extensionByContentType.isEmpty()) {
			return DEFAULT_EXTENSION_BY_CONTENT_TYPE;
		}
		return extensionByContentType;
	}
}
