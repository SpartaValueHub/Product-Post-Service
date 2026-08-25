package com.sparta.product_post_service.adaptor.out.s3;

import java.time.Duration;

import org.springframework.stereotype.Component;

import com.sparta.product_post_service.application.port.out.PresignObjectPutPort;
import com.sparta.product_post_service.config.MediaProperties;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Component
@RequiredArgsConstructor
public class S3PresignObjectPutAdapter implements PresignObjectPutPort {

	// AWS S3 Presigner
	private final S3Presigner s3Presigner;
	// 미디어 설정
	private final MediaProperties mediaProperties;

	@Override
	public String createPutUrl(String s3Key, String contentType, long contentLength, int expiresInSeconds) {
		PutObjectRequest putObjectRequest = PutObjectRequest.builder()
				.bucket(mediaProperties.s3Bucket())
				.key(s3Key)
				.contentType(contentType)
				.contentLength(contentLength)
				.build();

		PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
				.signatureDuration(Duration.ofSeconds(expiresInSeconds))
				.putObjectRequest(putObjectRequest)
				.build();

		return s3Presigner.presignPutObject(presignRequest).url().toString();
	}
}
