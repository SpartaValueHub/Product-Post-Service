package com.sparta.product_post_service.application.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sparta.product_post_service.support.SearchPropertiesTestFixture;

class SearchHourBucketSupportTest {

	@Test
	void recentHourBucketKeys_returnsNewestFirstInScoreZone() {
		Clock clock = Clock.fixed(Instant.parse("2026-03-26T03:30:00Z"), ZoneId.of("UTC"));
		SearchHourBucketSupport support = new SearchHourBucketSupport(
				SearchPropertiesTestFixture.minimal(),
				clock
		);

		List<String> keys = support.recentHourBucketKeys(2);

		// Asia/Seoul = UTC+9 → 2026-03-26 12시
		assertThat(keys).containsExactly(
				"search:terms:h:2026032612",
				"search:terms:h:2026032611"
		);
	}

	@Test
	void currentHourBucketKey_usesScoreZone() {
		Clock clock = Clock.fixed(Instant.parse("2026-03-26T03:30:00Z"), ZoneId.of("UTC"));
		SearchHourBucketSupport support = new SearchHourBucketSupport(
				SearchPropertiesTestFixture.minimal(),
				clock
		);

		assertThat(support.currentHourBucketKey()).isEqualTo("search:terms:h:2026032612");
	}
}
