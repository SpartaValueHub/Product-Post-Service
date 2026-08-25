package com.sparta.product_post_service.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sparta.product_post_service.application.exception.MediaConfigurationException;
import com.sparta.product_post_service.application.exception.MediaInvalidRequestException;
import com.sparta.product_post_service.application.exception.UnauthorizedException;
import com.sparta.product_post_service.application.port.in.dto.IssuePresignedUploadCommand;
import com.sparta.product_post_service.application.port.in.dto.IssuePresignedUploadResultDto;
import com.sparta.product_post_service.application.port.out.PresignObjectPutPort;
import com.sparta.product_post_service.config.MediaProperties;

@ExtendWith(MockitoExtension.class)
class IssuePresignedUploadServiceTest {

	private static final String MEMBER_UUID = "550e8400-e29b-41d4-a716-446655440000";

	@Mock
	private PresignObjectPutPort presignObjectPutPort;

	private IssuePresignedUploadService service;

	@BeforeEach
	void setUp() {
		MediaProperties mediaProperties = new MediaProperties(
				"valuehub-media-test",
				"https://dxxxx.cloudfront.net",
				"ap-northeast-2",
				5_242_880L,
				300
		);
		service = new IssuePresignedUploadService(presignObjectPutPort, mediaProperties);
	}

	@Test
	void issuePresignedUpload_returnsUploadAndPublicUrl() {
		when(presignObjectPutPort.createPutUrl(anyString(), eq("image/png"), eq(2048L), eq(300)))
				.thenReturn("https://s3.example/upload");

		IssuePresignedUploadResultDto result = service.issuePresignedUpload(
				IssuePresignedUploadCommand.builder()
						.memberUuid(MEMBER_UUID)
						.contentType("image/png")
						.contentLength(2048L)
						.build()
		);

		assertThat(result.getUploadUrl()).isEqualTo("https://s3.example/upload");
		assertThat(result.getS3Key()).startsWith("posts/" + MEMBER_UUID + "/");
		assertThat(result.getS3Key()).endsWith(".png");
		assertThat(result.getPublicUrl()).startsWith("https://dxxxx.cloudfront.net/posts/" + MEMBER_UUID + "/");
		assertThat(result.getExpiresInSeconds()).isEqualTo(300);
		verify(presignObjectPutPort).createPutUrl(anyString(), eq("image/png"), eq(2048L), eq(300));
	}

	@Test
	void issuePresignedUpload_rejectsInvalidContentType() {
		assertThatThrownBy(() -> service.issuePresignedUpload(
				IssuePresignedUploadCommand.builder()
						.memberUuid(MEMBER_UUID)
						.contentType("text/plain")
						.contentLength(100L)
						.build()
		))
				.isInstanceOf(MediaInvalidRequestException.class)
				.extracting(ex -> ((MediaInvalidRequestException) ex).getCode())
				.isEqualTo("INVALID_CONTENT_TYPE");
	}

	@Test
	void issuePresignedUpload_rejectsTooLargeContentLength() {
		assertThatThrownBy(() -> service.issuePresignedUpload(
				IssuePresignedUploadCommand.builder()
						.memberUuid(MEMBER_UUID)
						.contentType("image/gif")
						.contentLength(5_242_881L)
						.build()
		))
				.isInstanceOf(MediaInvalidRequestException.class)
				.extracting(ex -> ((MediaInvalidRequestException) ex).getCode())
				.isEqualTo("INVALID_CONTENT_LENGTH");
	}

	@Test
	void issuePresignedUpload_rejectsMissingMemberUuid() {
		assertThatThrownBy(() -> service.issuePresignedUpload(
				IssuePresignedUploadCommand.builder()
						.memberUuid(" ")
						.contentType("image/jpeg")
						.contentLength(100L)
						.build()
		))
				.isInstanceOf(UnauthorizedException.class);
	}

	@Test
	void issuePresignedUpload_rejectsMissingCloudFront() {
		IssuePresignedUploadService missingConfig = new IssuePresignedUploadService(
				presignObjectPutPort,
				new MediaProperties("bucket", "", "ap-northeast-2", 5_242_880L, 300)
		);

		assertThatThrownBy(() -> missingConfig.issuePresignedUpload(
				IssuePresignedUploadCommand.builder()
						.memberUuid(MEMBER_UUID)
						.contentType("image/webp")
						.contentLength(100L)
						.build()
		))
				.isInstanceOf(MediaConfigurationException.class)
				.extracting(ex -> ((MediaConfigurationException) ex).getCode())
				.isEqualTo("MEDIA_CONFIG_MISSING");
	}
}
