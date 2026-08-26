package com.sparta.product_post_service.application.service;

import org.springframework.stereotype.Service;

import com.sparta.product_post_service.application.exception.MediaConfigurationException;
import com.sparta.product_post_service.application.exception.MediaInvalidRequestException;
import com.sparta.product_post_service.application.exception.UnauthorizedException;
import com.sparta.product_post_service.application.port.in.IssuePresignedUploadUseCase;
import com.sparta.product_post_service.application.port.in.dto.IssuePresignedUploadCommand;
import com.sparta.product_post_service.application.port.in.dto.IssuePresignedUploadResultDto;
import com.sparta.product_post_service.application.port.out.PresignObjectPutPort;
import com.sparta.product_post_service.config.MediaProperties;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IssuePresignedUploadService implements IssuePresignedUploadUseCase {

	// S3 Presign Port
	private final PresignObjectPutPort presignObjectPutPort;
	// key 규칙
	private final MediaObjectKeyPolicy mediaObjectKeyPolicy;
	// 미디어 설정
	private final MediaProperties mediaProperties;

	@Override
	public IssuePresignedUploadResultDto issuePresignedUpload(IssuePresignedUploadCommand command) {
		String memberUuid = requireMemberUuid(command.getMemberUuid());
		String contentType = mediaObjectKeyPolicy.requireContentType(command.getContentType());
		long contentLength = requireContentLength(command.getContentLength());

		assertMediaConfigured();

		String s3Key = mediaObjectKeyPolicy.createPendingKey(memberUuid, contentType);
		int expiresInSeconds = mediaProperties.presignTtlSeconds() <= 0
				? 300
				: mediaProperties.presignTtlSeconds();
		String uploadUrl = presignObjectPutPort.createPutUrl(
				s3Key,
				contentType,
				contentLength,
				expiresInSeconds
		);

		return IssuePresignedUploadResultDto.builder()
				.uploadUrl(uploadUrl)
				.s3Key(s3Key)
				.publicUrl(mediaObjectKeyPolicy.toPublicUrl(s3Key))
				.expiresInSeconds(expiresInSeconds)
				.build();
	}

	private String requireMemberUuid(String memberUuid) {
		if (memberUuid == null || memberUuid.isBlank()) {
			throw new UnauthorizedException("판매자 정보가 없습니다.");
		}
		return memberUuid.trim();
	}

	private long requireContentLength(Long contentLength) {
		long maxBytes = mediaProperties.maxBytes() <= 0 ? 5_242_880L : mediaProperties.maxBytes();
		if (contentLength == null || contentLength <= 0 || contentLength > maxBytes) {
			throw new MediaInvalidRequestException(
					"INVALID_CONTENT_LENGTH",
					"파일 크기는 1바이트 이상 " + maxBytes + "바이트 이하여야 합니다."
			);
		}
		return contentLength;
	}

	private void assertMediaConfigured() {
		if (mediaProperties.s3Bucket() == null || mediaProperties.s3Bucket().isBlank()) {
			throw new MediaConfigurationException("MEDIA_CONFIG_MISSING", "S3_BUCKET이 설정되지 않았습니다.");
		}
		if (mediaProperties.cloudfrontBaseUrl() == null || mediaProperties.cloudfrontBaseUrl().isBlank()) {
			throw new MediaConfigurationException("MEDIA_CONFIG_MISSING", "CLOUDFRONT_BASE_URL이 설정되지 않았습니다.");
		}
	}
}
