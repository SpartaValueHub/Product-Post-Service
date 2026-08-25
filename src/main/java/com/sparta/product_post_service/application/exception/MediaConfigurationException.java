package com.sparta.product_post_service.application.exception;

// S3/CloudFront 설정 누락 등 미디어 인프라 설정 오류
public class MediaConfigurationException extends RuntimeException {

	// 에러 코드
	private final String code;

	public MediaConfigurationException(String code, String message) {
		super(message);
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
