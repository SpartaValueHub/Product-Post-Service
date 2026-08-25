package com.sparta.product_post_service.application.exception;

// 미디어 Presigned 요청 검증 실패 — code로 원인 구분
public class MediaInvalidRequestException extends RuntimeException {

	// 에러 코드 (INVALID_CONTENT_TYPE 등)
	private final String code;

	public MediaInvalidRequestException(String code, String message) {
		super(message);
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
