package com.sparta.product_post_service.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sparta.product_post_service.application.port.out.ProductPostLoadPort;
import com.sparta.product_post_service.application.port.out.ProductPostSavePort;
import com.sparta.product_post_service.domain.exception.ProductPostNotFoundException;
import com.sparta.product_post_service.domain.model.ProductPost;
import com.sparta.product_post_service.domain.model.ProductPostStatus;
import com.sparta.product_post_service.domain.model.TradeStatus;

@ExtendWith(MockitoExtension.class)
class ReservationEventTradeStatusServiceTest {

	private static final String PRODUCT_POST_UUID = "11111111-1111-1111-1111-111111111111";
	private static final Instant NOW = Instant.parse("2026-08-26T12:00:00Z");

	@Mock
	private ProductPostLoadPort productPostLoadPort;
	@Mock
	private ProductPostSavePort productPostSavePort;

	private ReservationEventTradeStatusService service;

	@BeforeEach
	void setUp() {
		service = new ReservationEventTradeStatusService(
				productPostLoadPort,
				productPostSavePort,
				Clock.fixed(NOW, ZoneOffset.UTC)
		);
	}

	@Test
	void apply_sellingToReserved_updatesTradeStatusOnly() {
		ProductPost post = restore(TradeStatus.SELLING, ProductPostStatus.PUBLIC, null);
		when(productPostLoadPort.findByUuid(PRODUCT_POST_UUID)).thenReturn(Optional.of(post));

		service.apply(PRODUCT_POST_UUID, TradeStatus.RESERVED);

		assertThat(post.getTradeStatus()).isEqualTo(TradeStatus.RESERVED);
		assertThat(post.getUpdatedAt()).isEqualTo(NOW);
		verify(productPostSavePort).updateTradeStatus(post);
		verify(productPostSavePort, never()).update(any());
	}

	@Test
	void apply_alreadyReserved_doesNotSave() {
		ProductPost post = restore(TradeStatus.RESERVED, ProductPostStatus.PUBLIC, null);
		when(productPostLoadPort.findByUuid(PRODUCT_POST_UUID)).thenReturn(Optional.of(post));

		service.apply(PRODUCT_POST_UUID, TradeStatus.RESERVED);

		verify(productPostSavePort, never()).updateTradeStatus(any());
	}

	@Test
	void apply_missingPost_throwsNotFound() {
		when(productPostLoadPort.findByUuid(PRODUCT_POST_UUID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.apply(PRODUCT_POST_UUID, TradeStatus.RESERVED))
				.isInstanceOf(ProductPostNotFoundException.class);
		verify(productPostSavePort, never()).updateTradeStatus(any());
	}

	@Test
	void apply_soldOut_throwsIllegalArgument() {
		ProductPost post = restore(TradeStatus.SOLD_OUT, ProductPostStatus.PUBLIC, null);
		when(productPostLoadPort.findByUuid(PRODUCT_POST_UUID)).thenReturn(Optional.of(post));

		assertThatThrownBy(() -> service.apply(PRODUCT_POST_UUID, TradeStatus.RESERVED))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("SOLD_OUT");
		verify(productPostSavePort, never()).updateTradeStatus(any());
	}

	@Test
	void apply_deletedPost_throwsNotFound() {
		ProductPost post = restore(TradeStatus.SELLING, ProductPostStatus.DELETED, NOW);
		when(productPostLoadPort.findByUuid(PRODUCT_POST_UUID)).thenReturn(Optional.of(post));

		assertThatThrownBy(() -> service.apply(PRODUCT_POST_UUID, TradeStatus.RESERVED))
				.isInstanceOf(ProductPostNotFoundException.class);
		verify(productPostSavePort, never()).updateTradeStatus(any());
	}

	private ProductPost restore(TradeStatus tradeStatus, ProductPostStatus productPostStatus, Instant deletedAt) {
		return ProductPost.restore(
				1L,
				PRODUCT_POST_UUID,
				"22222222-2222-2222-2222-222222222222",
				"33333333-3333-3333-3333-333333333333",
				"테스트 상품",
				"A",
				1_000_000L,
				"설명",
				tradeStatus,
				productPostStatus,
				new BigDecimal("37.5000000"),
				new BigDecimal("127.0000000"),
				"강남",
				"역삼동",
				"강남구",
				null,
				Instant.parse("2026-08-01T00:00:00Z"),
				null,
				deletedAt,
				List.of(),
				List.of()
		);
	}
}
