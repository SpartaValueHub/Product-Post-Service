package com.sparta.product_post_service.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sparta.product_post_service.application.port.in.dto.ListProductPostsQuery;
import com.sparta.product_post_service.application.port.in.dto.ProductPostCardPageDto;
import com.sparta.product_post_service.application.port.out.ProductPostLoadPort;
import com.sparta.product_post_service.application.port.out.dto.ProductPostCardPageProjection;
import com.sparta.product_post_service.application.port.out.dto.ProductPostCardProjection;
import com.sparta.product_post_service.application.port.out.dto.ProductPostListCriteria;
import com.sparta.product_post_service.application.support.SearchSessionKeyResolver;
import com.sparta.product_post_service.config.SearchProperties;
import com.sparta.product_post_service.domain.model.ProductPostStatus;
import com.sparta.product_post_service.domain.model.TradeStatus;

@ExtendWith(MockitoExtension.class)
class ProductPostQueryServiceListTest {

	@Mock
	private ProductPostLoadPort productPostLoadPort;

	@Mock
	private SearchTermRecordingService searchTermRecordingService;

	@Mock
	private SearchSessionKeyResolver searchSessionKeyResolver;

	@Mock
	private SearchProperties searchProperties;

	@InjectMocks
	private ProductPostQueryService productPostQueryService;

	@BeforeEach
	void setUp() {
		lenient().when(searchProperties.keywordMaxLength()).thenReturn(50);
		lenient().when(searchProperties.fulltextMinKeywordLength()).thenReturn(2);
		lenient().when(searchSessionKeyResolver.resolve(any(), any())).thenReturn(null);
	}

	@Test
	void list_withoutTradeStatus_passesAllVisibleStatuses() {
		stubEmptyPage();

		productPostQueryService.list(baseQuery(null).build());

		ProductPostListCriteria criteria = captureCriteria();
		assertThat(criteria.getProductPostStatus()).isEqualTo(ProductPostStatus.PUBLIC);
		assertThat(criteria.getTradeStatuses())
				.containsExactlyElementsOf(
						Arrays.stream(TradeStatus.values())
								.filter(TradeStatus::isListVisible)
								.toList()
				);
		verify(searchTermRecordingService, never()).recordAsync(anyString(), any());
	}

	@Test
	void list_withKeyword_normalizesAndRecordsAsync() {
		stubEmptyPage();

		productPostQueryService.list(baseQuery(null).keyword("  샤넬  백  ").build());

		ProductPostListCriteria criteria = captureCriteria();
		assertThat(criteria.getKeyword()).isEqualTo("샤넬 백");
		verify(searchTermRecordingService).recordAsync(eq("샤넬 백"), isNull());
	}

	@Test
	void list_withShortKeyword_returnsEmptyWithoutDbLookup() {
		ProductPostCardPageDto result = productPostQueryService.list(baseQuery(null).keyword("롤").build());

		assertThat(result.getContent()).isEmpty();
		assertThat(result.getTotalElements()).isZero();
		verify(productPostLoadPort, never()).findCards(any());
		verify(searchTermRecordingService).recordAsync(eq("롤"), isNull());
	}

	@Test
	void list_withSelling_passesSingleStatus() {
		stubEmptyPage();

		productPostQueryService.list(baseQuery("SELLING").memberUuid("member-1").build());

		ProductPostListCriteria criteria = captureCriteria();
		assertThat(criteria.getMemberUuid()).isEqualTo("member-1");
		assertThat(criteria.getTradeStatuses()).containsExactly(TradeStatus.SELLING);
	}

	@Test
	void list_withInvalidTradeStatus_throwsIllegalArgument() {
		assertThatThrownBy(() -> productPostQueryService.list(baseQuery("HIDDEN").build()))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("tradeStatus");
	}

	@Test
	void list_mapsRegionDongGuAndPlaceNameToCard() {
		when(productPostLoadPort.findCards(any())).thenReturn(
				ProductPostCardPageProjection.builder()
						.content(List.of(
								ProductPostCardProjection.builder()
										.productPostUuid("pp-1")
										.productPostName("가방")
										.price(1_000_000L)
										.tradeStatus(TradeStatus.SELLING)
										.listedAt(Instant.parse("2026-08-25T00:00:00Z"))
										.thumbnailUrl(null)
										.regionDong("초량동")
										.regionGu("동구")
										.placeName("부산역")
										.build()
						))
						.totalElements(1L)
						.page(0)
						.size(20)
						.build()
		);

		ProductPostCardPageDto result = productPostQueryService.list(baseQuery("SELLING").build());

		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).getRegionDong()).isEqualTo("초량동");
		assertThat(result.getContent().get(0).getRegionGu()).isEqualTo("동구");
		assertThat(result.getContent().get(0).getPlaceName()).isEqualTo("부산역");
	}

	@Test
	void list_totalPages_reflectsFilteredTotalElements() {
		when(productPostLoadPort.findCards(any())).thenReturn(
				ProductPostCardPageProjection.builder()
						.content(List.of(
								ProductPostCardProjection.builder()
										.productPostUuid("pp-1")
										.productPostName("가방")
										.price(1_000_000L)
										.tradeStatus(TradeStatus.SELLING)
										.listedAt(Instant.parse("2026-08-25T00:00:00Z"))
										.thumbnailUrl(null)
										.regionDong(null)
										.regionGu(null)
										.placeName("부산역")
										.build()
						))
						.totalElements(41L)
						.page(0)
						.size(20)
						.build()
		);

		ProductPostCardPageDto result = productPostQueryService.list(baseQuery("SELLING").page(1).size(20).build());

		assertThat(result.getTotalElements()).isEqualTo(41L);
		assertThat(result.getTotalPages()).isEqualTo(3);
		assertThat(result.getPage()).isEqualTo(1);
	}

	private ListProductPostsQuery.ListProductPostsQueryBuilder baseQuery(String tradeStatus) {
		return ListProductPostsQuery.builder()
				.tradeStatus(tradeStatus)
				.page(1)
				.size(20);
	}

	private void stubEmptyPage() {
		when(productPostLoadPort.findCards(any())).thenReturn(
				ProductPostCardPageProjection.builder()
						.content(List.of())
						.totalElements(0L)
						.page(0)
						.size(20)
						.build()
		);
	}

	private ProductPostListCriteria captureCriteria() {
		ArgumentCaptor<ProductPostListCriteria> captor = ArgumentCaptor.forClass(ProductPostListCriteria.class);
		verify(productPostLoadPort).findCards(captor.capture());
		return captor.getValue();
	}
}
