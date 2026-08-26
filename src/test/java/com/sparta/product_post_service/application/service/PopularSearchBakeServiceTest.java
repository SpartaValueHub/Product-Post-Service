package com.sparta.product_post_service.application.service;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
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

import com.sparta.product_post_service.application.port.out.LoadSearchTermRankingPort;
import com.sparta.product_post_service.application.port.out.SavePopularSearchTermsPort;
import com.sparta.product_post_service.application.port.out.SearchBakeLockPort;
import com.sparta.product_post_service.config.SearchProperties;

@ExtendWith(MockitoExtension.class)
class PopularSearchBakeServiceTest {

	@Mock
	private LoadSearchTermRankingPort loadSearchTermRankingPort;

	@Mock
	private SavePopularSearchTermsPort savePopularSearchTermsPort;

	@Mock
	private SearchBakeLockPort searchBakeLockPort;

	private PopularSearchBakeService popularSearchBakeService;

	@BeforeEach
	void setUp() {
		popularSearchBakeService = new PopularSearchBakeService(
				loadSearchTermRankingPort,
				savePopularSearchTermsPort,
				searchBakeLockPort,
				minimalSearchProperties()
		);
	}

	@Test
	void bake_whenLockAcquired_savesTopTerms() {
		when(searchBakeLockPort.tryLock()).thenReturn(true);
		when(loadSearchTermRankingPort.loadTopTerms(5, 1.0d))
				.thenReturn(List.of("롤렉스", "샤넬백"));

		popularSearchBakeService.bake();

		verify(savePopularSearchTermsPort).savePopular(List.of("롤렉스", "샤넬백"));
		verify(searchBakeLockPort).unlock();
	}

	@Test
	void bake_whenLockNotAcquired_skips() {
		when(searchBakeLockPort.tryLock()).thenReturn(false);

		popularSearchBakeService.bake();

		verify(loadSearchTermRankingPort, never()).loadTopTerms(anyInt(), anyDouble());
		verify(savePopularSearchTermsPort, never()).savePopular(anyList());
		verify(searchBakeLockPort, never()).unlock();
	}

	private static SearchProperties minimalSearchProperties() {
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
				List.of("롤렉스"),
				Map.of()
		);
	}
}
