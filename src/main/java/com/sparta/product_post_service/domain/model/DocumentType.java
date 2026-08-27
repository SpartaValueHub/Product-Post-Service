package com.sparta.product_post_service.domain.model;

import java.util.Arrays;
import java.util.stream.Collectors;

// 판매 서류 종류 (보증서·영수증·감정서·기타)
public enum DocumentType {
	WARRANTY,
	RECEIPT,
	APPRAISAL,
	OTHER;

	// 종류별 첨부 상한 (등록·수정 공통)
	public static final int MAX_COUNT_PER_TYPE = 2;

	// 전체 상한 = 종류 수 × 종류별 상한
	public static int maxTotalCount() {
		return values().length * MAX_COUNT_PER_TYPE;
	}

	// 허용 값 안내 문구 (에러 메시지용)
	public static String allowedNamesCsv() {
		return Arrays.stream(values())
				.map(Enum::name)
				.collect(Collectors.joining(", "));
	}
}
