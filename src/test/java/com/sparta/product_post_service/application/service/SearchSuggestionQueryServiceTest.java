package com.sparta.product_post_service.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
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

import com.sparta.product_post_service.application.port.out.LoadFallbackSuggestionTermsPort;
import com.sparta.product_post_service.application.port.out.LoadSuggestionTermsPort;
import com.sparta.product_post_service.application.support.SuggestionPrefixMatcher;
import com.sparta.product_post_service.support.SearchPropertiesTestFixture;

@ExtendWith(MockitoExtension.class)
class SearchSuggestionQueryServiceTest {

	@Mock
	private LoadSuggestionTermsPort loadSuggestionTermsPort;

	@Mock
	private LoadFallbackSuggestionTermsPort loadFallbackSuggestionTermsPort;

	private SearchSuggestionQueryService searchSuggestionQueryService;

	@BeforeEach
	void setUp() {
		searchSuggestionQueryService = new SearchSuggestionQueryService(
				loadSuggestionTermsPort,
				loadFallbackSuggestionTermsPort,
				new SuggestionPrefixMatcher(),
				SearchPropertiesTestFixture.withPopularSeedAndRelated(
						List.of("롤렉스", "샤넬백"),
						Map.of("샤넬백", List.of("샤넬백 보증서"))
				)
		);
	}

	@Test
	void getSuggestions_whenRedisHasPrefix_returnsRedis() {
		when(loadSuggestionTermsPort.loadByPrefix("롤렉", 5))
				.thenReturn(List.of("롤렉스", "롤렉스 서브마리너"));

		List<String> terms = searchSuggestionQueryService.getSuggestions(" 롤렉 ");

		assertThat(terms).containsExactly("롤렉스", "롤렉스 서브마리너");
		verify(loadFallbackSuggestionTermsPort, never()).loadCandidates();
	}

	@Test
	void getSuggestions_whenRedisEmpty_fallsBackToLocalPrefix() {
		when(loadSuggestionTermsPort.loadByPrefix("샤넬", 5)).thenReturn(List.of());
		when(loadFallbackSuggestionTermsPort.loadCandidates())
				.thenReturn(List.of("롤렉스", "샤넬백", "샤넬백 보증서"));

		List<String> terms = searchSuggestionQueryService.getSuggestions("샤넬");

		assertThat(terms).containsExactly("샤넬백", "샤넬백 보증서");
	}

	@Test
	void getSuggestions_blank_returnsEmpty() {
		assertThat(searchSuggestionQueryService.getSuggestions("   ")).isEmpty();
		verify(loadSuggestionTermsPort, never()).loadByPrefix(anyString(), anyInt());
	}

	@Test
	void getSuggestions_shorterThanMinLength_returnsEmpty() {
		assertThat(searchSuggestionQueryService.getSuggestions("샤")).isEmpty();
		verify(loadSuggestionTermsPort, never()).loadByPrefix(anyString(), anyInt());
	}
}
