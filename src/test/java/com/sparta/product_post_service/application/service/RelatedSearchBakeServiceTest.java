package com.sparta.product_post_service.application.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sparta.product_post_service.application.port.out.LoadSearchCooccurrencePort;
import com.sparta.product_post_service.application.port.out.RelatedSearchBakeLockPort;
import com.sparta.product_post_service.application.port.out.SaveRelatedSearchTermsPort;
import com.sparta.product_post_service.config.SearchProperties;

@ExtendWith(MockitoExtension.class)
class RelatedSearchBakeServiceTest {

	@Mock
	private LoadSearchCooccurrencePort loadSearchCooccurrencePort;

	@Mock
	private SaveRelatedSearchTermsPort saveRelatedSearchTermsPort;

	@Mock
	private RelatedSearchBakeLockPort relatedSearchBakeLockPort;

	private RelatedSearchBakeService relatedSearchBakeService;

	@BeforeEach
	void setUp() {
		relatedSearchBakeService = new RelatedSearchBakeService(
				loadSearchCooccurrencePort,
				saveRelatedSearchTermsPort,
				relatedSearchBakeLockPort,
				new SearchProperties(
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
						List.of(),
						Map.of()
				)
		);
	}

	@Test
	void bake_whenLockAcquired_savesRelatedExcludingSelf() {
		when(relatedSearchBakeLockPort.tryLock()).thenReturn(true);
		when(loadSearchCooccurrencePort.loadSourceTerms()).thenReturn(Set.of("샤넬백"));
		when(loadSearchCooccurrencePort.loadTopRelated(eq("샤넬백"), anyInt(), anyDouble()))
				.thenReturn(List.of("샤넬백", "샤넬백 보증서", "샤넬 클래식"));

		relatedSearchBakeService.bake();

		verify(saveRelatedSearchTermsPort).saveRelated("샤넬백", List.of("샤넬백 보증서", "샤넬 클래식"));
		verify(relatedSearchBakeLockPort).unlock();
	}

	@Test
	void bake_whenLockNotAcquired_skips() {
		when(relatedSearchBakeLockPort.tryLock()).thenReturn(false);

		relatedSearchBakeService.bake();

		verify(loadSearchCooccurrencePort, never()).loadSourceTerms();
		verify(saveRelatedSearchTermsPort, never()).saveRelated(anyString(), any());
		verify(relatedSearchBakeLockPort, never()).unlock();
	}
}
