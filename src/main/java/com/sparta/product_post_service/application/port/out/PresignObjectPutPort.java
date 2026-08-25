package com.sparta.product_post_service.application.port.out;

// S3 Presigned PUT URL 발급
public interface PresignObjectPutPort {

	// contentType·contentLength를 바인딩한 Presigned PUT URL 반환
	String createPutUrl(String s3Key, String contentType, long contentLength, int expiresInSeconds);
}
