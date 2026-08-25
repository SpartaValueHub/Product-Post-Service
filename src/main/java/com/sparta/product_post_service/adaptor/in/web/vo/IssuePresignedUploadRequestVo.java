package com.sparta.product_post_service.adaptor.in.web.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "이미지 Presigned URL 발급 요청")
public class IssuePresignedUploadRequestVo {

	@Schema(description = "업로드 Content-Type", example = "image/jpeg")
	private String contentType;

	@Schema(description = "업로드 파일 바이트 크기", example = "1048576")
	private Long contentLength;
}
