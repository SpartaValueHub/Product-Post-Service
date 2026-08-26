package com.sparta.product_post_service.adaptor.in.kafka;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sparta.product_post_service.adaptor.in.kafka.vo.ReservationEventPayload;
import com.sparta.product_post_service.application.port.in.ApplyReservationEventTradeStatusUseCase;
import com.sparta.product_post_service.application.service.ReservationEventTradeStatusResolver;
import com.sparta.product_post_service.domain.exception.ProductPostNotFoundException;
import com.sparta.product_post_service.domain.model.TradeStatus;

@ExtendWith(MockitoExtension.class)
class ReservationEventListenerTest {

	private static final String PRODUCT_POST_UUID = "11111111-1111-1111-1111-111111111111";

	@Mock
	private ReservationEventPayloadMapper reservationEventPayloadMapper;
	@Mock
	private ReservationEventTradeStatusResolver reservationEventTradeStatusResolver;
	@Mock
	private ApplyReservationEventTradeStatusUseCase applyReservationEventTradeStatusUseCase;

	private ReservationEventListener listener;

	@BeforeEach
	void setUp() {
		listener = new ReservationEventListener(
				reservationEventPayloadMapper,
				reservationEventTradeStatusResolver,
				applyReservationEventTradeStatusUseCase
		);
	}

	@Test
	void consume_created_appliesReserved() {
		when(reservationEventPayloadMapper.parse("payload"))
				.thenReturn(Optional.of(new ReservationEventPayload("CREATED", PRODUCT_POST_UUID)));
		when(reservationEventTradeStatusResolver.resolve("CREATED")).thenReturn(Optional.of(TradeStatus.RESERVED));

		listener.consume("payload");

		verify(applyReservationEventTradeStatusUseCase).apply(PRODUCT_POST_UUID, TradeStatus.RESERVED);
	}

	@Test
	void consume_unmappedEventType_skipsUseCase() {
		when(reservationEventPayloadMapper.parse("payload"))
				.thenReturn(Optional.of(new ReservationEventPayload("UPDATED", PRODUCT_POST_UUID)));
		when(reservationEventTradeStatusResolver.resolve("UPDATED")).thenReturn(Optional.empty());

		listener.consume("payload");

		verify(applyReservationEventTradeStatusUseCase, never()).apply(anyString(), any());
	}

	@Test
	void consume_invalidJson_skipsUseCase() {
		when(reservationEventPayloadMapper.parse("not-json")).thenReturn(Optional.empty());

		listener.consume("not-json");

		verify(applyReservationEventTradeStatusUseCase, never()).apply(anyString(), any());
	}

	@Test
	void consume_missingPost_skipsWithoutRethrow() {
		when(reservationEventPayloadMapper.parse("payload"))
				.thenReturn(Optional.of(new ReservationEventPayload("CREATED", PRODUCT_POST_UUID)));
		when(reservationEventTradeStatusResolver.resolve("CREATED")).thenReturn(Optional.of(TradeStatus.RESERVED));
		doThrow(new ProductPostNotFoundException("판매글을 찾을 수 없습니다."))
				.when(applyReservationEventTradeStatusUseCase)
				.apply(PRODUCT_POST_UUID, TradeStatus.RESERVED);

		listener.consume("payload");

		verify(applyReservationEventTradeStatusUseCase).apply(PRODUCT_POST_UUID, TradeStatus.RESERVED);
	}
}
