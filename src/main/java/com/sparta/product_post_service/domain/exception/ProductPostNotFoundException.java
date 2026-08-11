package com.sparta.product_post_service.domain.exception;

// 판매글을 찾을 수 없거나 일반 사용자에게 노출할 수 없을 때
public class ProductPostNotFoundException extends RuntimeException {

	// 안정적 에러 코드
	public static final String CODE = "PRODUCT_POST_NOT_FOUND";

	public ProductPostNotFoundException(String message) {
		super(message);
	}

	// 에러 코드 반환
	public String getCode() {
		return CODE;
	}
}
