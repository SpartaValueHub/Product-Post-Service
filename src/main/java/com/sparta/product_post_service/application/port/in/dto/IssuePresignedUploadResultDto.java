package com.sparta.product_post_service.application.port.in.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IssuePresignedUploadResultDto {

	// 클라이언트가 S3에 PUT할 URL
	private final String uploadUrl;
	// S3 object key
	private final String s3Key;
	// CloudFront 공개 URL
	private final String publicUrl;
	// Presigned 유효 초
	private final int expiresInSeconds;
}
