package com.sparta.product_post_service.adaptor.in.web.validation;

import com.sparta.product_post_service.config.ProductPostPolicyProperties;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

// product-post.policy.min-price 설정을 읽어 가격 하한 검증
public class MinProductPostPriceValidator implements ConstraintValidator<MinProductPostPrice, Long> {

	// 판매글 정책 설정
	private final ProductPostPolicyProperties productPostPolicyProperties;

	public MinProductPostPriceValidator(ProductPostPolicyProperties productPostPolicyProperties) {
		this.productPostPolicyProperties = productPostPolicyProperties;
	}

	@Override
	public boolean isValid(Long price, ConstraintValidatorContext context) {
		if (price == null) {
			return true;
		}
		long minPrice = productPostPolicyProperties.minPrice();
		if (price >= minPrice) {
			return true;
		}
		context.disableDefaultConstraintViolation();
		context.buildConstraintViolationWithTemplate("가격은 " + minPrice + "원 이상이어야 합니다.")
				.addConstraintViolation();
		return false;
	}
}
