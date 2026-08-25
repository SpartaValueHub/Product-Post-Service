package com.sparta.product_post_service.adaptor.in.web.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "이미지 Presigned URL 발급 응답")
public class IssuePresignedUploadResponseVo {

	@Schema(description = "클라이언트가 S3에 PUT할 URL")
	private String uploadUrl;

	@Schema(description = "S3 object key")
	private String s3Key;

	@Schema(description = "CloudFront 공개 URL")
	private String publicUrl;

	@Schema(description = "Presigned URL 유효 초", example = "300")
	private int expiresInSeconds;
}
