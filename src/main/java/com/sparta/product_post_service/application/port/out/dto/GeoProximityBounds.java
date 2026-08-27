package com.sparta.product_post_service.application.port.out.dto;

// 반경 검색용 위·경도 바운딩 박스
public record GeoProximityBounds(
		double minLatitude,
		double maxLatitude,
		double minLongitude,
		double maxLongitude
) {
}
