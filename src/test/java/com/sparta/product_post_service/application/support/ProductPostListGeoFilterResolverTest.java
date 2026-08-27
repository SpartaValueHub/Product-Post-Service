package com.sparta.product_post_service.application.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sparta.product_post_service.application.port.out.dto.ProductPostListGeoFilter;
import com.sparta.product_post_service.config.ProductPostPolicyProperties;

class ProductPostListGeoFilterResolverTest {

	private ProductPostListGeoFilterResolver resolver;

	@BeforeEach
	void setUp() {
		ProductPostPolicyProperties policy = new ProductPostPolicyProperties(500_000L, 2, 12L, 3D);
		resolver = new ProductPostListGeoFilterResolver(policy, new GeoProximityBoundsCalculator());
	}

	@Test
	void resolve_withMemberUuid_disablesDistanceFilter() {
		ProductPostListGeoFilter filter = resolver.resolve("member-1", null, null, null);

		assertThat(filter.isDistanceFilterEnabled()).isFalse();
		assertThat(filter.isMissingCenterCoordinates()).isFalse();
	}

	@Test
	void resolve_withoutMemberUuidAndWithoutCenter_returnsMissingCenter() {
		ProductPostListGeoFilter filter = resolver.resolve(null, null, null, null);

		assertThat(filter.isMissingCenterCoordinates()).isTrue();
		assertThat(filter.isDistanceFilterEnabled()).isFalse();
	}

	@Test
	void resolve_withCenter_appliesDefaultRadiusFromPolicy() {
		ProductPostListGeoFilter filter = resolver.resolve(null, 35.1159D, 129.0403D, null);

		assertThat(filter.isDistanceFilterEnabled()).isTrue();
		assertThat(filter.getRadiusKm()).isEqualTo(3D);
		assertThat(filter.getCenterLatitude()).isEqualTo(35.1159D);
		assertThat(filter.getMinLatitude()).isLessThan(filter.getCenterLatitude());
		assertThat(filter.getMaxLatitude()).isGreaterThan(filter.getCenterLatitude());
	}

	@Test
	void resolve_withPartialCenter_throwsIllegalArgument() {
		assertThatThrownBy(() -> resolver.resolve(null, 35.0D, null, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("centerLatitude");
	}
}
