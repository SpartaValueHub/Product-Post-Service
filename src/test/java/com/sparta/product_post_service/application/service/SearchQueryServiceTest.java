package com.sparta.product_post_service.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sparta.product_post_service.application.port.out.LoadPopularSearchTermsPort;
import com.sparta.product_post_service.application.port.out.LoadRelatedSearchTermsPort;
import com.sparta.product_post_service.config.SearchProperties;

@ExtendWith(MockitoExtension.class)
class SearchQueryServiceTest {

	@Mock
	private LoadPopularSearchTermsPort loadPopularSearchTermsPort;

	@Mock
	private LoadRelatedSearchTermsPort loadRelatedSearchTermsPort;

	private SearchQueryService searchQueryService;

	@BeforeEach
	void setUp() {
		SearchProperties properties = new SearchProperties(
				10,
				50,
				"search:terms:z",
				List.of("롤렉스", "샤넬백", "빈티지 백"),
				Map.of(
						"샤넬백", List.of("샤넬백 보증서", "샤넬 클래식"),
						"롤렉스", List.of("롤렉스 서브마리너")
				)
		);
		searchQueryService = new SearchQueryService(
				loadPopularSearchTermsPort,
				loadRelatedSearchTermsPort,
				properties
		);
	}

	@Test
	void getPopular_whenRedisEmpty_returnsSeed() {
		when(loadPopularSearchTermsPort.loadPopular(10)).thenReturn(List.of());

		List<String> terms = searchQueryService.getPopular();

		assertThat(terms).containsExactly("롤렉스", "샤넬백", "빈티지 백");
	}

	@Test
	void getPopular_whenRedisHasTerms_returnsRedisTop() {
		when(loadPopularSearchTermsPort.loadPopular(10)).thenReturn(List.of("오메가", "에르메스"));

		List<String> terms = searchQueryService.getPopular();

		assertThat(terms).containsExactly("오메가", "에르메스");
	}

	@Test
	void getRelated_returnsDictionaryTerms() {
		when(loadRelatedSearchTermsPort.loadRelated("샤넬백"))
				.thenReturn(List.of("샤넬백 보증서", "샤넬 클래식"));

		List<String> terms = searchQueryService.getRelated("  샤넬백  ");

		assertThat(terms).containsExactly("샤넬백 보증서", "샤넬 클래식");
		verifyNoInteractions(loadPopularSearchTermsPort);
	}

	@Test
	void getRelated_whenDictionaryEmpty_fallsBackToPopular() {
		when(loadRelatedSearchTermsPort.loadRelated("없는검색어")).thenReturn(List.of());
		when(loadPopularSearchTermsPort.loadPopular(10)).thenReturn(List.of("롤렉스", "샤넬백"));

		List<String> terms = searchQueryService.getRelated("없는검색어");

		assertThat(terms).containsExactly("롤렉스", "샤넬백");
	}

	@Test
	void getRelated_blankQuery_returnsEmpty() {
		List<String> terms = searchQueryService.getRelated("   ");

		assertThat(terms).isEmpty();
		verifyNoInteractions(loadRelatedSearchTermsPort);
		verifyNoInteractions(loadPopularSearchTermsPort);
	}
}
