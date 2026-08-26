package com.sparta.product_post_service.application.support;

import java.util.Locale;

// 검색어 정규화 (공백 축약·소문자·길이 제한)
public final class SearchTermNormalizer {

	private SearchTermNormalizer() {
	}

	// blank면 null. maxLength 초과 시 truncate
	public static String normalize(String raw, int maxLength) {
		if (raw == null || raw.isBlank()) {
			return null;
		}
		String normalized = raw.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
		if (normalized.isEmpty()) {
			return null;
		}
		if (maxLength > 0 && normalized.length() > maxLength) {
			return normalized.substring(0, maxLength);
		}
		return normalized;
	}
}
