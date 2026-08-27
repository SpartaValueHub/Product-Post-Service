package com.sparta.product_post_service.application.port.out.dto;

import lombok.Builder;
import lombok.Getter;

// 목록 반경 필터 (거래희망장소 ↔ 조회 중심점)
@Getter
@Builder
public class ProductPostListGeoFilter {

	// 반경 조건 적용 여부 (false면 memberUuid 전용 조회 등)
	private final boolean distanceFilterEnabled;
	// 중심 좌표 미전달로 빈 목록 반환
	private final boolean missingCenterCoordinates;
	// Haversine 중심 위도
	private final double centerLatitude;
	// Haversine 중심 경도
	private final double centerLongitude;
	// 반경 (km)
	private final double radiusKm;
	// 바운딩 박스 최소 위도
	private final double minLatitude;
	// 바운딩 박스 최대 위도
	private final double maxLatitude;
	// 바운딩 박스 최소 경도
	private final double minLongitude;
	// 바운딩 박스 최대 경도
	private final double maxLongitude;

	// 판매자 필터 등 — 거리 조건 없음
	public static ProductPostListGeoFilter disabled() {
		return ProductPostListGeoFilter.builder()
				.distanceFilterEnabled(false)
				.missingCenterCoordinates(false)
				.centerLatitude(0D)
				.centerLongitude(0D)
				.radiusKm(0D)
				.minLatitude(0D)
				.maxLatitude(0D)
				.minLongitude(0D)
				.maxLongitude(0D)
				.build();
	}

	// 일반 목록인데 중심 좌표 없음 — 빈 결과
	public static ProductPostListGeoFilter missingCenter() {
		return ProductPostListGeoFilter.builder()
				.distanceFilterEnabled(false)
				.missingCenterCoordinates(true)
				.centerLatitude(0D)
				.centerLongitude(0D)
				.radiusKm(0D)
				.minLatitude(0D)
				.maxLatitude(0D)
				.minLongitude(0D)
				.maxLongitude(0D)
				.build();
	}

	// 거리 필터 적용
	public static ProductPostListGeoFilter active(
			double centerLatitude,
			double centerLongitude,
			double radiusKm,
			double minLatitude,
			double maxLatitude,
			double minLongitude,
			double maxLongitude
	) {
		return ProductPostListGeoFilter.builder()
				.distanceFilterEnabled(true)
				.missingCenterCoordinates(false)
				.centerLatitude(centerLatitude)
				.centerLongitude(centerLongitude)
				.radiusKm(radiusKm)
				.minLatitude(minLatitude)
				.maxLatitude(maxLatitude)
				.minLongitude(minLongitude)
				.maxLongitude(maxLongitude)
				.build();
	}
}
