package com.sparta.product_post_service.application.port.in.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IssuePresignedUploadCommand {

	// 요청 회원 UUID (경로 소유자)
	private final String memberUuid;
	// 업로드 Content-Type
	private final String contentType;
	// 업로드 바이트 크기
	private final Long contentLength;
}
