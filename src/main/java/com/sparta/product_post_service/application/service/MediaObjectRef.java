package com.sparta.product_post_service.application.service;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

// Presign·승격 대상 객체 해석 결과
@Getter
public class MediaObjectRef {

	// 객체 상태
	public enum State {
		PENDING,
		CONFIRMED,
		PASSTHROUGH
	}

	// PENDING / CONFIRMED / PASSTHROUGH
	private final State state;
	// pending 원본 key (PENDING만)
	private final String sourceKey;
	// 정식 key (PENDING·CONFIRMED)
	private final String destinationKey;
	// DB에 넣을 공개 URL 또는 통과 URL
	private final String publicUrl;

	public static MediaObjectRef pending(String sourceKey, String destinationKey, String publicUrl) {
		return MediaObjectRef.builder()
				.state(State.PENDING)
				.sourceKey(sourceKey)
				.destinationKey(destinationKey)
				.publicUrl(publicUrl)
				.build();
	}

	public static MediaObjectRef confirmed(String destinationKey, String publicUrl) {
		return MediaObjectRef.builder()
				.state(State.CONFIRMED)
				.destinationKey(destinationKey)
				.publicUrl(publicUrl)
				.build();
	}

	public static MediaObjectRef passthrough(String publicUrl) {
		return MediaObjectRef.builder()
				.state(State.PASSTHROUGH)
				.publicUrl(publicUrl)
				.build();
	}

	public boolean isPending() {
		return state == State.PENDING;
	}

	@Builder(access = AccessLevel.PRIVATE)
	private MediaObjectRef(State state, String sourceKey, String destinationKey, String publicUrl) {
		this.state = state;
		this.sourceKey = sourceKey;
		this.destinationKey = destinationKey;
		this.publicUrl = publicUrl;
	}
}
