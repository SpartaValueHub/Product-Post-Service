package com.sparta.product_post_service.application.exception;

// 권한 없음 (타인 리소스 접근 등)
public class ForbiddenException extends RuntimeException {

	// 표준 에러 코드
	public static final String CODE = "FORBIDDEN";

	public ForbiddenException(String message) {
		super(message);
	}

	public String getCode() {
		return CODE;
	}
}
