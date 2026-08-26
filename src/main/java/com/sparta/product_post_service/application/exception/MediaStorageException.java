package com.sparta.product_post_service.application.exception;

// 객체 저장소 Copy·Delete 등 인프라 실패
public class MediaStorageException extends RuntimeException {

	// 안정적 에러 코드
	private final String code;

	public MediaStorageException(String code, String message) {
		super(message);
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
