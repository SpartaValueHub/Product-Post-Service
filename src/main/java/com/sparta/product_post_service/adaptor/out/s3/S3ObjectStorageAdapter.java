package com.sparta.product_post_service.adaptor.out.s3;

import org.springframework.stereotype.Component;

import com.sparta.product_post_service.application.exception.MediaStorageException;
import com.sparta.product_post_service.application.port.out.ObjectStoragePort;
import com.sparta.product_post_service.config.MediaProperties;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.MetadataDirective;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Component
@RequiredArgsConstructor
public class S3ObjectStorageAdapter implements ObjectStoragePort {

	// AWS S3 Client
	private final S3Client s3Client;
	// 미디어 설정
	private final MediaProperties mediaProperties;

	@Override
	public void copyObject(String sourceKey, String destinationKey) {
		try {
			s3Client.copyObject(CopyObjectRequest.builder()
					.sourceBucket(mediaProperties.s3Bucket())
					.sourceKey(sourceKey)
					.destinationBucket(mediaProperties.s3Bucket())
					.destinationKey(destinationKey)
					.metadataDirective(MetadataDirective.COPY)
					.build());
		} catch (S3Exception ex) {
			throw new MediaStorageException("MEDIA_STORAGE_FAILED", "미디어 승격에 실패했습니다.");
		}
	}

	@Override
	public boolean exists(String objectKey) {
		try {
			s3Client.headObject(HeadObjectRequest.builder()
					.bucket(mediaProperties.s3Bucket())
					.key(objectKey)
					.build());
			return true;
		} catch (NoSuchKeyException ex) {
			return false;
		} catch (S3Exception ex) {
			if (ex.statusCode() == 404) {
				return false;
			}
			throw new MediaStorageException("MEDIA_STORAGE_FAILED", "미디어 조회에 실패했습니다.");
		}
	}

	@Override
	public void deleteObject(String objectKey) {
		try {
			s3Client.deleteObject(DeleteObjectRequest.builder()
					.bucket(mediaProperties.s3Bucket())
					.key(objectKey)
					.build());
		} catch (S3Exception ex) {
			throw new MediaStorageException("MEDIA_STORAGE_FAILED", "미디어 삭제에 실패했습니다.");
		}
	}
}
