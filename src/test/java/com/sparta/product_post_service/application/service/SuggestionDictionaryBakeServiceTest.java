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
import com.sparta.product_post_service.application.port.out.LoadFallbackSuggestionTermsPort;
import com.sparta.product_post_service.application.port.out.SaveSuggestionDictionaryPort;
import com.sparta.product_post_service.application.port.out.SuggestionDictionaryBakeLockPort;
import com.sparta.product_post_service.application.support.SearchHourBucketSupport;
import com.sparta.product_post_service.support.SearchPropertiesTestFixture;

@ExtendWith(MockitoExtension.class)
class SuggestionDictionaryBakeServiceTest {

	@Mock
	private AggregateSearchTermCountsPort aggregateSearchTermCountsPort;

	@Mock
	private LoadFallbackSuggestionTermsPort loadFallbackSuggestionTermsPort;

	@Mock
	private SaveSuggestionDictionaryPort saveSuggestionDictionaryPort;

	@Mock
	private SuggestionDictionaryBakeLockPort suggestionDictionaryBakeLockPort;

	@Mock
	private SearchHourBucketSupport searchHourBucketSupport;

	private SuggestionDictionaryBakeService suggestionDictionaryBakeService;

	@BeforeEach
	void setUp() {
		suggestionDictionaryBakeService = new SuggestionDictionaryBakeService(
				aggregateSearchTermCountsPort,
				loadFallbackSuggestionTermsPort,
				saveSuggestionDictionaryPort,
				suggestionDictionaryBakeLockPort,
				searchHourBucketSupport,
				SearchPropertiesTestFixture.minimal()
		);
	}

	@Test
	void bake_whenLockAcquired_savesMergedDictionary() {
		when(suggestionDictionaryBakeLockPort.tryLock()).thenReturn(true);
		when(searchHourBucketSupport.recentHourBucketKeys(168))
				.thenReturn(List.of("search:terms:h:2026032612"));
		when(aggregateSearchTermCountsPort.aggregateTopCounts(anyList(), eq(2000)))
				.thenReturn(Map.of("오메가", 10.0d));
		when(loadFallbackSuggestionTermsPort.loadCandidates())
				.thenReturn(List.of("롤렉스", "샤넬백"));

		suggestionDictionaryBakeService.bake();

		verify(saveSuggestionDictionaryPort).saveDictionary(List.of("오메가", "롤렉스", "샤넬백"));
		verify(suggestionDictionaryBakeLockPort).unlock();
	}

	@Test
	void bake_whenLockNotAcquired_skips() {
		when(suggestionDictionaryBakeLockPort.tryLock()).thenReturn(false);

		suggestionDictionaryBakeService.bake();

		verify(aggregateSearchTermCountsPort, never()).aggregateTopCounts(anyList(), anyInt());
		verify(saveSuggestionDictionaryPort, never()).saveDictionary(any());
		verify(suggestionDictionaryBakeLockPort, never()).unlock();
	}
}
