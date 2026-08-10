package com.sparta.listing_service.adaptor.in.web.validation;

import com.sparta.listing_service.config.ListingPolicyProperties;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

// listing.policy.min-price 설정을 읽어 가격 하한 검증
public class MinListingPriceValidator implements ConstraintValidator<MinListingPrice, Long> {

	// 판매글 정책 설정
	private final ListingPolicyProperties listingPolicyProperties;

	public MinListingPriceValidator(ListingPolicyProperties listingPolicyProperties) {
		this.listingPolicyProperties = listingPolicyProperties;
	}

	@Override
	public boolean isValid(Long price, ConstraintValidatorContext context) {
		if (price == null) {
			return true;
		}
		long minPrice = listingPolicyProperties.minPrice();
		if (price >= minPrice) {
			return true;
		}
		context.disableDefaultConstraintViolation();
		context.buildConstraintViolationWithTemplate("가격은 " + minPrice + "원 이상이어야 합니다.")
				.addConstraintViolation();
		return false;
	}
}
