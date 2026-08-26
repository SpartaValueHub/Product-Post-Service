package com.sparta.product_post_service.application.port.out;

// 객체 저장소 Copy·Exists·Delete (S3 등)
public interface ObjectStoragePort {

	// 원본 key를 대상 key로 복사한다. Content-Type 메타데이터를 유지한다.
	void copyObject(String sourceKey, String destinationKey);

	// key 존재 여부
	boolean exists(String objectKey);

	// key 삭제. 대상이 없어도 성공으로 본다.
	void deleteObject(String objectKey);
}
