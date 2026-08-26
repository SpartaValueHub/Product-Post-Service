package com.sparta.product_post_service.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sparta.product_post_service.application.exception.MediaInvalidRequestException;
import com.sparta.product_post_service.application.port.out.ObjectStoragePort;
import com.sparta.product_post_service.config.MediaProperties;

@ExtendWith(MockitoExtension.class)
class PromotePendingMediaServiceTest {

	private static final String MEMBER_UUID = "550e8400-e29b-41d4-a716-446655440000";

	@Mock
	private ObjectStoragePort objectStoragePort;

	private PromotePendingMediaService service;

	@BeforeEach
	void setUp() {
		service = new PromotePendingMediaService(
				new MediaObjectKeyPolicy(new MediaProperties(
						"valuehub-media-test",
						"https://dxxxx.cloudfront.net",
						"ap-northeast-2",
						5_242_880L,
						300,
						"pending/",
						"posts/",
						java.util.Map.of("image/jpeg", "jpg"),
						java.util.List.of()
				)),
				objectStoragePort
		);
	}

	@Test
	void persistAll_copiesPendingThenDeletesSource() {
		String pendingUrl = "https://dxxxx.cloudfront.net/pending/posts/" + MEMBER_UUID + "/a1.jpg";
		when(objectStoragePort.exists("pending/posts/" + MEMBER_UUID + "/a1.jpg")).thenReturn(true);

		List<String> result = service.persistAll(MEMBER_UUID, List.of(pendingUrl));

		assertThat(result).containsExactly("https://dxxxx.cloudfront.net/posts/" + MEMBER_UUID + "/a1.jpg");
		verify(objectStoragePort).copyObject(
				"pending/posts/" + MEMBER_UUID + "/a1.jpg",
				"posts/" + MEMBER_UUID + "/a1.jpg"
		);
		verify(objectStoragePort).deleteObject("pending/posts/" + MEMBER_UUID + "/a1.jpg");
	}

	@Test
	void persistAll_skipsCopyWhenAlreadyConfirmed() {
		String confirmedUrl = "https://dxxxx.cloudfront.net/posts/" + MEMBER_UUID + "/a1.jpg";

		List<String> result = service.persistAll(MEMBER_UUID, List.of(confirmedUrl));

		assertThat(result).containsExactly(confirmedUrl);
		verify(objectStoragePort, never()).copyObject(anyString(), anyString());
	}

	@Test
	void persistAll_rollsBackCopiedDestinationsWhenLaterCopyFails() {
		String first = "https://dxxxx.cloudfront.net/pending/posts/" + MEMBER_UUID + "/a1.jpg";
		String second = "https://dxxxx.cloudfront.net/pending/posts/" + MEMBER_UUID + "/a2.jpg";
		when(objectStoragePort.exists("pending/posts/" + MEMBER_UUID + "/a1.jpg")).thenReturn(true);
		when(objectStoragePort.exists("pending/posts/" + MEMBER_UUID + "/a2.jpg")).thenReturn(false);
		when(objectStoragePort.exists("posts/" + MEMBER_UUID + "/a2.jpg")).thenReturn(false);

		assertThatThrownBy(() -> service.persistAll(MEMBER_UUID, List.of(first, second)))
				.isInstanceOf(MediaInvalidRequestException.class);

		verify(objectStoragePort).deleteObject("posts/" + MEMBER_UUID + "/a1.jpg");
	}
}
