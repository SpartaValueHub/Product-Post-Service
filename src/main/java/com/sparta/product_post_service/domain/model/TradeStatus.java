package com.sparta.product_post_service.domain.model;

// 거래 상태
public enum TradeStatus {
	SELLING(true),
	RESERVED(true),
	SOLD_OUT(true);

	// FO 공개 목록에 노출할지
	private final boolean listVisible;

	TradeStatus(boolean listVisible) {
		this.listVisible = listVisible;
	}

	// FO 공개 목록 노출 여부
	public boolean isListVisible() {
		return listVisible;
	}
}
