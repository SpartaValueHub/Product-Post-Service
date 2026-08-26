package com.sparta.product_post_service.application.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sparta.product_post_service.application.port.out.AggregateSearchTermCountsPort;
import com.sparta.product_post_service.application.port.out.SavePopularSearchTermsPort;
import com.sparta.product_post_service.application.port.out.SearchBakeLockPort;
import com.sparta.product_post_service.application.support.PopularSearchScoreCalculator;
import com.sparta.product_post_service.application.support.SearchHourBucketSupport;
import com.sparta.product_post_service.support.SearchPropertiesTestFixture;

@ExtendWith(MockitoExtension.class)
class PopularSearchBakeServiceTest {

	@Mock
	private AggregateSearchTermCountsPort aggregateSearchTermCountsPort;

	@Mock
	private SavePopularSearchTermsPort savePopularSearchTermsPort;

	@Mock
	private SearchBakeLockPort searchBakeLockPort;

	@Mock
	private SearchHourBucketSupport searchHourBucketSupport;

	private PopularSearchBakeService popularSearchBakeService;

	@BeforeEach
	void setUp() {
		var properties = SearchPropertiesTestFixture.minimal();
		popularSearchBakeService = new PopularSearchBakeService(
				aggregateSearchTermCountsPort,
				savePopularSearchTermsPort,
				searchBakeLockPort,
				searchHourBucketSupport,
				new PopularSearchScoreCalculator(properties),
				properties
		);
	}

	@Test
	void bake_whenLockAcquired_savesWeightedTopTerms() {
		when(searchBakeLockPort.tryLock()).thenReturn(true);
		when(searchHourBucketSupport.recentHourBucketKeys(24))
				.thenReturn(List.of("search:terms:h:2026032612"));
		when(searchHourBucketSupport.recentHourBucketKeys(168))
				.thenReturn(List.of("search:terms:h:2026032612", "search:terms:h:2026031912"));
		when(aggregateSearchTermCountsPort.aggregateTopCounts(anyList(), eq(500)))
				.thenReturn(Map.of("롤렉스", 10.0d, "샤넬백", 2.0d))
				.thenReturn(Map.of("롤렉스", 12.0d, "샤넬백", 20.0d, "오메가", 100.0d));

		popularSearchBakeService.bake();

		// 오메가: 0*3 + 100*1 = 100, 롤렉스: 10*3 + 2*1 = 32, 샤넬백: 2*3 + 18*1 = 24
		verify(savePopularSearchTermsPort).savePopular(List.of("오메가", "롤렉스", "샤넬백"));
		verify(searchBakeLockPort).unlock();
	}

	@Test
	void bake_whenLockNotAcquired_skips() {
		when(searchBakeLockPort.tryLock()).thenReturn(false);

		popularSearchBakeService.bake();

		verify(aggregateSearchTermCountsPort, never()).aggregateTopCounts(anyList(), anyInt());
		verify(savePopularSearchTermsPort, never()).savePopular(any());
		verify(searchBakeLockPort, never()).unlock();
	}
}
