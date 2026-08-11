package com.sparta.product_post_service.application.exception;

// 인증·판매자 식별 실패
public class UnauthorizedException extends RuntimeException {

	// 안정적 에러 코드
	public static final String CODE = "UNAUTHORIZED";

	public UnauthorizedException(String message) {
		super(message);
	}

	// 에러 코드 반환
	public String getCode() {
		return CODE;
	}
}
