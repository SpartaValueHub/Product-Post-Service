package com.sparta.product_post_service.application.support;

import org.springframework.stereotype.Component;

import com.sparta.product_post_service.application.port.out.dto.GeoProximityBounds;

// 지구 반경(km) 기준 바운딩 박스 계산 (Haversine 전 1차 필터)
@Component
public class GeoProximityBoundsCalculator {

	// 위도 1도당 대략 km (적도 기준)
	private static final double KM_PER_LATITUDE_DEGREE = 111.0D;

	// 중심·반경(km) → 위·경도 min/max
	public GeoProximityBounds calculate(double centerLatitude, double centerLongitude, double radiusKm) {
		double latitudeDelta = radiusKm / KM_PER_LATITUDE_DEGREE;
		double longitudeDelta = radiusKm / (KM_PER_LATITUDE_DEGREE * Math.cos(Math.toRadians(centerLatitude)));
		return new GeoProximityBounds(
				centerLatitude - latitudeDelta,
				centerLatitude + latitudeDelta,
				centerLongitude - longitudeDelta,
				centerLongitude + longitudeDelta
		);
	}
}
