package com.sparta.listing_service.adaptor.in.web.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

// 설정(product-post.policy.min-price) 기준 최소가 검증
@Documented
@Constraint(validatedBy = MinProductPostPriceValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MinProductPostPrice {

	String message() default "가격이 최소 판매가 미만입니다.";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
