package com.sparta.product_post_service.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.sparta.product_post_service.application.exception.MediaInvalidRequestException;
import com.sparta.product_post_service.application.port.out.ObjectStoragePort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// pending 객체를 정식 prefix로 승격하고 DB용 publicUrl을 만든다.
@Slf4j
@Service
@RequiredArgsConstructor
public class PromotePendingMediaService {

	// key·URL 규칙
	private final MediaObjectKeyPolicy mediaObjectKeyPolicy;
	// 객체 저장소
	private final ObjectStoragePort objectStoragePort;

	// 여러 URL을 모두 검증한 뒤 승격한다. 일부 실패 시 이번 요청에서 만든 정식 객체를 되돌린다.
	public List<String> persistAll(String memberUuid, List<String> requestedUrlOrKeys) {
		if (requestedUrlOrKeys == null || requestedUrlOrKeys.isEmpty()) {
			return List.of();
		}
		List<MediaObjectRef> refs = requestedUrlOrKeys.stream()
				.map(url -> mediaObjectKeyPolicy.resolve(memberUuid, url))
				.toList();

		List<String> copiedDestinations = new ArrayList<>();
		try {
			for (MediaObjectRef ref : refs) {
				if (ref.isPending() && copyPending(ref)) {
					copiedDestinations.add(ref.getDestinationKey());
				}
			}
		} catch (RuntimeException ex) {
			copiedDestinations.forEach(this::deleteQuietly);
			throw ex;
		}

		refs.stream()
				.filter(MediaObjectRef::isPending)
				.map(MediaObjectRef::getSourceKey)
				.filter(Objects::nonNull)
				.forEach(this::deleteQuietly);

		return refs.stream().map(MediaObjectRef::getPublicUrl).toList();
	}

	// 본인 정식 객체를 삭제한다. 실패해도 업무 저장은 유지한다.
	public void deleteConfirmedIfOwned(String memberUuid, String previousUrlOrKey) {
		mediaObjectKeyPolicy.confirmedKeyIfOwned(memberUuid, previousUrlOrKey)
				.ifPresent(this::deleteQuietly);
	}

	// source가 있으면 Copy. 이미 dest만 있으면 재시도로 보고 skip. 둘 다 없으면 400.
	private boolean copyPending(MediaObjectRef ref) {
		if (objectStoragePort.exists(ref.getSourceKey())) {
			objectStoragePort.copyObject(ref.getSourceKey(), ref.getDestinationKey());
			return true;
		}
		if (objectStoragePort.exists(ref.getDestinationKey())) {
			return false;
		}
		throw new MediaInvalidRequestException("MEDIA_OBJECT_NOT_FOUND", "업로드된 파일을 찾을 수 없습니다.");
	}

	private void deleteQuietly(String objectKey) {
		try {
			objectStoragePort.deleteObject(objectKey);
		} catch (RuntimeException ex) {
			log.warn("미디어 객체 삭제 실패 key={}", objectKey, ex);
		}
	}
}
