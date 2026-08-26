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

import com.sparta.product_post_service.application.port.out.LoadBakedRelatedSearchTermsPort;
import com.sparta.product_post_service.application.port.out.LoadDictionaryRelatedSearchTermsPort;
import com.sparta.product_post_service.application.port.out.LoadPopularSearchTermsPort;
import com.sparta.product_post_service.config.SearchProperties;

@ExtendWith(MockitoExtension.class)
class SearchQueryServiceTest {

	@Mock
	private LoadPopularSearchTermsPort loadPopularSearchTermsPort;

	@Mock
	private LoadBakedRelatedSearchTermsPort loadBakedRelatedSearchTermsPort;

	@Mock
	private LoadDictionaryRelatedSearchTermsPort loadDictionaryRelatedSearchTermsPort;

	private SearchQueryService searchQueryService;

	@BeforeEach
	void setUp() {
		searchQueryService = new SearchQueryService(
				loadPopularSearchTermsPort,
				loadBakedRelatedSearchTermsPort,
				loadDictionaryRelatedSearchTermsPort,
				testSearchProperties()
		);
	}

	@Test
	void getPopular_whenRedisEmpty_returnsSeed() {
		when(loadPopularSearchTermsPort.loadPopular(5)).thenReturn(List.of());

		List<String> terms = searchQueryService.getPopular();

		assertThat(terms).containsExactly("롤렉스", "샤넬백", "빈티지 백");
	}

	@Test
	void getPopular_whenRedisHasTerms_returnsRedisTop() {
		when(loadPopularSearchTermsPort.loadPopular(5)).thenReturn(List.of("오메가", "에르메스"));

		List<String> terms = searchQueryService.getPopular();

		assertThat(terms).containsExactly("오메가", "에르메스");
	}

	@Test
	void getRelated_prefersBakedOverDictionary() {
		when(loadBakedRelatedSearchTermsPort.loadRelated("샤넬백", 5))
				.thenReturn(List.of("샤넬백 A급"));

		List<String> terms = searchQueryService.getRelated("  샤넬백  ");

		assertThat(terms).containsExactly("샤넬백 A급");
		verifyNoInteractions(loadDictionaryRelatedSearchTermsPort);
		verifyNoInteractions(loadPopularSearchTermsPort);
	}

	@Test
	void getRelated_whenBakedEmpty_usesDictionary() {
		when(loadBakedRelatedSearchTermsPort.loadRelated("샤넬백", 5)).thenReturn(List.of());
		when(loadDictionaryRelatedSearchTermsPort.loadRelated("샤넬백"))
				.thenReturn(List.of("샤넬백 보증서", "샤넬 클래식"));

		List<String> terms = searchQueryService.getRelated("  샤넬백  ");

		assertThat(terms).containsExactly("샤넬백 보증서", "샤넬 클래식");
		verifyNoInteractions(loadPopularSearchTermsPort);
	}

	@Test
	void getRelated_whenDictionaryEmpty_fallsBackToPopular() {
		when(loadBakedRelatedSearchTermsPort.loadRelated("없는검색어", 5)).thenReturn(List.of());
		when(loadDictionaryRelatedSearchTermsPort.loadRelated("없는검색어")).thenReturn(List.of());
		when(loadPopularSearchTermsPort.loadPopular(5)).thenReturn(List.of("롤렉스", "샤넬백"));

		List<String> terms = searchQueryService.getRelated("없는검색어");

		assertThat(terms).containsExactly("롤렉스", "샤넬백");
	}

	@Test
	void getRelated_blankQuery_returnsEmpty() {
		List<String> terms = searchQueryService.getRelated("   ");

		assertThat(terms).isEmpty();
		verifyNoInteractions(loadBakedRelatedSearchTermsPort);
		verifyNoInteractions(loadDictionaryRelatedSearchTermsPort);
		verifyNoInteractions(loadPopularSearchTermsPort);
	}

	private static SearchProperties testSearchProperties() {
		return new SearchProperties(
				5,
				50,
				2,
				"search:terms:z",
				"search:popular",
				":building",
				3_600_000L,
				60_000L,
				1.0d,
				1.0d,
				"search:popular:bake-lock",
				300_000L,
				"1",
				"search:cooc:z:",
				"search:cooc:sources",
				1.0d,
				"search:related:",
				2.0d,
				"search:related:bake-lock",
				"search:session:last:",
				"m:",
				"s:",
				1_800_000L,
				List.of("롤렉스", "샤넬백", "빈티지 백"),
				Map.of(
						"샤넬백", List.of("샤넬백 보증서", "샤넬 클래식"),
						"롤렉스", List.of("롤렉스 서브마리너")
				)
		);
	}
}
