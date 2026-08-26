package com.sparta.product_post_service.application.service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.sparta.product_post_service.application.exception.ForbiddenException;
import com.sparta.product_post_service.application.exception.MediaInvalidRequestException;
import com.sparta.product_post_service.config.MediaProperties;

import lombok.RequiredArgsConstructor;

// 판매글 미디어 key·URL 규칙 (pending 발급·승격 해석)
@Component
@RequiredArgsConstructor
public class MediaObjectKeyPolicy {

	// 미디어 설정
	private final MediaProperties mediaProperties;

	// 허용 Content-Type을 정규화해 반환한다.
	public String requireContentType(String contentType) {
		String normalized = normalizeContentType(contentType);
		if (!extensionByContentType().containsKey(normalized)) {
			throw new MediaInvalidRequestException("INVALID_CONTENT_TYPE", "허용되지 않는 Content-Type입니다.");
		}
		return normalized;
	}

	// Presign용 pending key 생성
	public String createPendingKey(String memberUuid, String contentType) {
		String normalized = requireContentType(contentType);
		String extension = extensionByContentType().get(normalized);
		return pendingPrefix() + confirmedPrefix() + memberUuid + "/" + UUID.randomUUID() + "." + extension;
	}

	// 요청 URL/key를 본인 미디어로 해석한다.
	public MediaObjectRef resolve(String memberUuid, String requestedUrlOrKey) {
		String trimmed = trimToNull(requestedUrlOrKey);
		if (trimmed == null) {
			throw new MediaInvalidRequestException("INVALID_MEDIA_KEY", "미디어 URL이 올바르지 않습니다.");
		}
		if (isPassthrough(trimmed)) {
			return MediaObjectRef.passthrough(trimmed);
		}

		String objectKey = extractObjectKey(trimmed);
		assertSafeKey(objectKey);

		String ownedPendingPrefix = pendingPrefix() + confirmedPrefix() + memberUuid + "/";
		String ownedConfirmedPrefix = confirmedPrefix() + memberUuid + "/";
		String anyPendingPrefix = pendingPrefix() + confirmedPrefix();
		String anyConfirmedPrefix = confirmedPrefix();

		if (objectKey.startsWith(ownedPendingPrefix) && isSafeFileName(objectKey.substring(ownedPendingPrefix.length()))) {
			String destinationKey = objectKey.substring(pendingPrefix().length());
			return MediaObjectRef.pending(objectKey, destinationKey, toPublicUrl(destinationKey));
		}
		if (objectKey.startsWith(ownedConfirmedPrefix) && isSafeFileName(objectKey.substring(ownedConfirmedPrefix.length()))) {
			return MediaObjectRef.confirmed(objectKey, toPublicUrl(objectKey));
		}
		if (objectKey.startsWith(anyPendingPrefix) || objectKey.startsWith(anyConfirmedPrefix)) {
			throw new ForbiddenException("해당 미디어에 대한 권한이 없습니다.");
		}
		throw new MediaInvalidRequestException("INVALID_MEDIA_KEY", "미디어 URL이 올바르지 않습니다.");
	}

	// 본인 정식 key면 반환 (삭제 대상 판별)
	public Optional<String> confirmedKeyIfOwned(String memberUuid, String urlOrKey) {
		String trimmed = trimToNull(urlOrKey);
		if (trimmed == null || isPassthrough(trimmed)) {
			return Optional.empty();
		}
		String objectKey;
		try {
			objectKey = extractObjectKey(trimmed);
			assertSafeKey(objectKey);
		} catch (MediaInvalidRequestException ex) {
			return Optional.empty();
		}
		String ownedConfirmedPrefix = confirmedPrefix() + memberUuid + "/";
		if (objectKey.startsWith(pendingPrefix())) {
			return Optional.empty();
		}
		if (objectKey.startsWith(ownedConfirmedPrefix) && isSafeFileName(objectKey.substring(ownedConfirmedPrefix.length()))) {
			return Optional.of(objectKey);
		}
		return Optional.empty();
	}

	// CloudFront publicUrl 조립
	public String toPublicUrl(String objectKey) {
		String base = normalizedCloudfrontBase();
		return base + "/" + objectKey;
	}

	private String normalizeContentType(String contentType) {
		if (contentType == null || contentType.isBlank()) {
			throw new MediaInvalidRequestException("INVALID_CONTENT_TYPE", "허용되지 않는 Content-Type입니다.");
		}
		String normalized = contentType.trim().toLowerCase(Locale.ROOT);
		int separator = normalized.indexOf(';');
		if (separator >= 0) {
			normalized = normalized.substring(0, separator).trim();
		}
		return normalized;
	}

	// empty map이면 jpeg/png/webp/gif 기본값을 쓴다
	Map<String, String> extensionByContentType() {
		Map<String, String> configured = mediaProperties.resolvedExtensionByContentType();
		if (configured == null || configured.isEmpty()) {
			return MediaProperties.DEFAULT_EXTENSION_BY_CONTENT_TYPE;
		}
		return configured;
	}

	private boolean isPassthrough(String value) {
		List<String> passthroughUrls = mediaProperties.passthroughUrls();
		if (passthroughUrls == null || passthroughUrls.isEmpty()) {
			return false;
		}
		return passthroughUrls.stream().anyMatch(value::equals);
	}

	private String extractObjectKey(String urlOrKey) {
		String withoutQuery = stripQueryAndFragment(urlOrKey);
		String base = normalizedCloudfrontBase();
		if (!base.isEmpty() && withoutQuery.startsWith(base + "/")) {
			return withoutQuery.substring(base.length() + 1);
		}
		if (withoutQuery.startsWith("http://") || withoutQuery.startsWith("https://")) {
			throw new MediaInvalidRequestException("INVALID_MEDIA_KEY", "미디어 URL이 올바르지 않습니다.");
		}
		return withoutQuery.startsWith("/") ? withoutQuery.substring(1) : withoutQuery;
	}

	private String stripQueryAndFragment(String value) {
		int query = value.indexOf('?');
		int fragment = value.indexOf('#');
		int end = value.length();
		if (query >= 0) {
			end = Math.min(end, query);
		}
		if (fragment >= 0) {
			end = Math.min(end, fragment);
		}
		return value.substring(0, end);
	}

	private void assertSafeKey(String objectKey) {
		if (objectKey == null || objectKey.isBlank() || objectKey.contains("..") || objectKey.startsWith("/")) {
			throw new MediaInvalidRequestException("INVALID_MEDIA_KEY", "미디어 URL이 올바르지 않습니다.");
		}
	}

	private boolean isSafeFileName(String fileName) {
		return fileName != null
				&& !fileName.isBlank()
				&& !fileName.contains("/")
				&& !fileName.contains("\\")
				&& !fileName.contains("..")
				&& fileName.indexOf('.') > 0
				&& fileName.indexOf('.') < fileName.length() - 1;
	}

	private String pendingPrefix() {
		return ensureTrailingSlash(mediaProperties.pendingPrefix(), "pending/");
	}

	private String confirmedPrefix() {
		return ensureTrailingSlash(mediaProperties.confirmedPrefix(), "posts/");
	}

	private String normalizedCloudfrontBase() {
		String base = mediaProperties.cloudfrontBaseUrl();
		if (base == null || base.isBlank()) {
			return "";
		}
		String trimmed = base.trim();
		if (trimmed.endsWith("/")) {
			return trimmed.substring(0, trimmed.length() - 1);
		}
		return trimmed;
	}

	private String ensureTrailingSlash(String value, String fallback) {
		String raw = (value == null || value.isBlank()) ? fallback : value.trim();
		return raw.endsWith("/") ? raw : raw + "/";
	}

	private String trimToNull(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}
}
