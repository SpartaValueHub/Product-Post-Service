package com.sparta.product_post_service.application.service;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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

	// 허용 Content-Type
	private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
			"image/jpeg",
			"image/png",
			"image/webp",
			"image/gif"
	);

	// Content-Type → 확장자
	private static final Map<String, String> EXTENSION_BY_CONTENT_TYPE = Map.of(
			"image/jpeg", "jpg",
			"image/png", "png",
			"image/webp", "webp",
			"image/gif", "gif"
	);

	// S3 Presign Port
	private final PresignObjectPutPort presignObjectPutPort;
	// 미디어 설정
	private final MediaProperties mediaProperties;

	@Override
	public IssuePresignedUploadResultDto issuePresignedUpload(IssuePresignedUploadCommand command) {
		String memberUuid = requireMemberUuid(command.getMemberUuid());
		String contentType = normalizeContentType(command.getContentType());
		long contentLength = requireContentLength(command.getContentLength());

		assertMediaConfigured();

		String extension = EXTENSION_BY_CONTENT_TYPE.get(contentType);
		String s3Key = "posts/" + memberUuid + "/" + UUID.randomUUID() + "." + extension;
		int expiresInSeconds = mediaProperties.presignTtlSeconds() <= 0
				? 300
				: mediaProperties.presignTtlSeconds();
		String uploadUrl = presignObjectPutPort.createPutUrl(
				s3Key,
				contentType,
				contentLength,
				expiresInSeconds
		);
		String publicUrl = buildPublicUrl(s3Key);

		return IssuePresignedUploadResultDto.builder()
				.uploadUrl(uploadUrl)
				.s3Key(s3Key)
				.publicUrl(publicUrl)
				.expiresInSeconds(expiresInSeconds)
				.build();
	}

	private String requireMemberUuid(String memberUuid) {
		if (memberUuid == null || memberUuid.isBlank()) {
			throw new UnauthorizedException("판매자 정보가 없습니다.");
		}
		return memberUuid.trim();
	}

	private String normalizeContentType(String contentType) {
		if (contentType == null || contentType.isBlank()) {
			throw new MediaInvalidRequestException("INVALID_CONTENT_TYPE", "허용되지 않는 Content-Type입니다.");
		}
		String normalized = contentType.trim().toLowerCase(Locale.ROOT);
		if (!ALLOWED_CONTENT_TYPES.contains(normalized)) {
			throw new MediaInvalidRequestException("INVALID_CONTENT_TYPE", "허용되지 않는 Content-Type입니다.");
		}
		return normalized;
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

	private String buildPublicUrl(String s3Key) {
		String base = mediaProperties.cloudfrontBaseUrl().trim();
		if (base.endsWith("/")) {
			base = base.substring(0, base.length() - 1);
		}
		return base + "/" + s3Key;
	}
}
