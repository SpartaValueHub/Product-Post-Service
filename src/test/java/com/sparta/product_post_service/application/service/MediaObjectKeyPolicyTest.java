package com.sparta.product_post_service.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sparta.product_post_service.application.exception.ForbiddenException;
import com.sparta.product_post_service.application.exception.MediaInvalidRequestException;
import com.sparta.product_post_service.config.MediaProperties;

class MediaObjectKeyPolicyTest {

	private static final String MEMBER_UUID = "550e8400-e29b-41d4-a716-446655440000";
	private static final String OTHER_UUID = "11111111-1111-1111-1111-111111111111";

	private MediaObjectKeyPolicy policy;

	@BeforeEach
	void setUp() {
		policy = new MediaObjectKeyPolicy(new MediaProperties(
				"valuehub-media-test",
				"https://dxxxx.cloudfront.net",
				"ap-northeast-2",
				5_242_880L,
				300,
				"pending/",
				"posts/",
				java.util.Map.of("image/jpeg", "jpg", "image/png", "png"),
				java.util.List.of()
		));
	}

	@Test
	void createPendingKey_usesPendingPostsPrefix() {
		String key = policy.createPendingKey(MEMBER_UUID, "image/png");

		assertThat(key).startsWith("pending/posts/" + MEMBER_UUID + "/");
		assertThat(key).endsWith(".png");
	}

	@Test
	void resolve_promotesOwnPendingUrlToConfirmed() {
		MediaObjectRef ref = policy.resolve(
				MEMBER_UUID,
				"https://dxxxx.cloudfront.net/pending/posts/" + MEMBER_UUID + "/a1.jpg"
		);

		assertThat(ref.isPending()).isTrue();
		assertThat(ref.getDestinationKey()).isEqualTo("posts/" + MEMBER_UUID + "/a1.jpg");
		assertThat(ref.getPublicUrl()).isEqualTo("https://dxxxx.cloudfront.net/posts/" + MEMBER_UUID + "/a1.jpg");
	}

	@Test
	void resolve_rejectsOtherMemberPending() {
		assertThatThrownBy(() -> policy.resolve(
				MEMBER_UUID,
				"https://dxxxx.cloudfront.net/pending/posts/" + OTHER_UUID + "/a1.jpg"
		))
				.isInstanceOf(ForbiddenException.class);
	}

	@Test
	void resolve_rejectsNonMediaUrl() {
		assertThatThrownBy(() -> policy.resolve(MEMBER_UUID, "https://cdn.example.com/product-posts/1.jpg"))
				.isInstanceOf(MediaInvalidRequestException.class)
				.extracting(ex -> ((MediaInvalidRequestException) ex).getCode())
				.isEqualTo("INVALID_MEDIA_KEY");
	}
}
