package com.sparta.product_post_service.application.support;

import org.springframework.stereotype.Component;

import com.sparta.product_post_service.application.port.out.dto.GeoProximityBounds;
import com.sparta.product_post_service.application.port.out.dto.ProductPostListGeoFilter;
import com.sparta.product_post_service.config.ProductPostPolicyProperties;

// 목록 반경 필터 해석 (memberUuid·좌표·설정 반경)
@Component
public class ProductPostListGeoFilterResolver {

	// 판매글 정책 (기본 반경 km)
	private final ProductPostPolicyProperties productPostPolicyProperties;
	// 중심점·반경 → 바운딩 박스
	private final GeoProximityBoundsCalculator geoProximityBoundsCalculator;

	public ProductPostListGeoFilterResolver(
			ProductPostPolicyProperties productPostPolicyProperties,
			GeoProximityBoundsCalculator geoProximityBoundsCalculator
	) {
		this.productPostPolicyProperties = productPostPolicyProperties;
		this.geoProximityBoundsCalculator = geoProximityBoundsCalculator;
	}

	// memberUuid 있으면 스킵, 없으면 좌표 필수(미전달 시 빈 목록)
	public ProductPostListGeoFilter resolve(
			String memberUuid,
			Double centerLatitude,
			Double centerLongitude,
			Double radiusKm
	) {
		if (memberUuid != null) {
			return ProductPostListGeoFilter.disabled();
		}
		if (centerLatitude == null && centerLongitude == null) {
			return ProductPostListGeoFilter.missingCenter();
		}
		if (centerLatitude == null || centerLongitude == null) {
			throw new IllegalArgumentException("centerLatitude와 centerLongitude는 함께 전달해야 합니다.");
		}
		double resolvedRadiusKm = radiusKm != null ? radiusKm : productPostPolicyProperties.searchRadiusKm();
		validateRadiusKm(resolvedRadiusKm);
		validateCoordinate(centerLatitude, "centerLatitude");
		validateCoordinate(centerLongitude, "centerLongitude");

		GeoProximityBounds bounds = geoProximityBoundsCalculator.calculate(
				centerLatitude,
				centerLongitude,
				resolvedRadiusKm
		);
		return ProductPostListGeoFilter.active(
				centerLatitude,
				centerLongitude,
				resolvedRadiusKm,
				bounds.minLatitude(),
				bounds.maxLatitude(),
				bounds.minLongitude(),
				bounds.maxLongitude()
		);
	}

	private void validateRadiusKm(double radiusKm) {
		if (radiusKm <= 0D) {
			throw new IllegalArgumentException("radiusKm는 0보다 커야 합니다.");
		}
	}

	private void validateCoordinate(double value, String fieldName) {
		if (fieldName.equals("centerLatitude") && (value < -90D || value > 90D)) {
			throw new IllegalArgumentException("centerLatitude는 -90~90 범위여야 합니다.");
		}
		if (fieldName.equals("centerLongitude") && (value < -180D || value > 180D)) {
			throw new IllegalArgumentException("centerLongitude는 -180~180 범위여야 합니다.");
		}
	}
}
