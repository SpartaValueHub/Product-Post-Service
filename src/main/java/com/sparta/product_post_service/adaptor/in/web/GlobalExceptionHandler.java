package com.sparta.product_post_service.adaptor.in.web;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.sparta.product_post_service.application.exception.ForbiddenException;
import com.sparta.product_post_service.application.exception.UnauthorizedException;
import com.sparta.product_post_service.domain.exception.ProductPostNotFoundException;

import jakarta.servlet.http.HttpServletRequest;

// API 예외 → 표준 Error Response 변환 (홀 안내판)
@RestControllerAdvice
public class GlobalExceptionHandler {

	// Bean Validation 실패
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValid(
			MethodArgumentNotValidException ex,
			HttpServletRequest request
	) {
		List<Map<String, String>> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
				.map(this::toFieldError)
				.toList();

		Map<String, Object> body = baseError(
				HttpStatus.BAD_REQUEST,
				"VALIDATION_FAILED",
				"요청 값이 올바르지 않습니다.",
				request.getRequestURI()
		);
		body.put("fieldErrors", fieldErrors);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}

	// JSON 파싱·타입 오류 (예: enum 값 오류)
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadable(
			HttpMessageNotReadableException ex,
			HttpServletRequest request
	) {
		return buildError(
				HttpStatus.BAD_REQUEST,
				"INVALID_ARGUMENT",
				"요청 본문을 읽을 수 없습니다.",
				request.getRequestURI()
		);
	}

	// Domain·Application 인자 오류
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, Object>> handleIllegalArgument(
			IllegalArgumentException ex,
			HttpServletRequest request
	) {
		return buildError(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", ex.getMessage(), request.getRequestURI());
	}

	// 인증·판매자 헤더 누락
	@ExceptionHandler(UnauthorizedException.class)
	public ResponseEntity<Map<String, Object>> handleUnauthorized(
			UnauthorizedException ex,
			HttpServletRequest request
	) {
		return buildError(HttpStatus.UNAUTHORIZED, ex.getCode(), ex.getMessage(), request.getRequestURI());
	}

	// 권한 없음 (타인 글 수정 등)
	@ExceptionHandler(ForbiddenException.class)
	public ResponseEntity<Map<String, Object>> handleForbidden(
			ForbiddenException ex,
			HttpServletRequest request
	) {
		return buildError(HttpStatus.FORBIDDEN, ex.getCode(), ex.getMessage(), request.getRequestURI());
	}

	// 판매글 없음·숨김·삭제 (일반 사용자 미노출)
	@ExceptionHandler(ProductPostNotFoundException.class)
	public ResponseEntity<Map<String, Object>> handleProductPostNotFound(
			ProductPostNotFoundException ex,
			HttpServletRequest request
	) {
		return buildError(HttpStatus.NOT_FOUND, ex.getCode(), ex.getMessage(), request.getRequestURI());
	}

	// fieldErrors 한 건 변환
	private Map<String, String> toFieldError(FieldError fieldError) {
		Map<String, String> item = new LinkedHashMap<>();
		item.put("field", fieldError.getField());
		item.put("code", fieldError.getCode() == null ? "INVALID" : fieldError.getCode());
		item.put("message", fieldError.getDefaultMessage() == null ? "값이 올바르지 않습니다." : fieldError.getDefaultMessage());
		return item;
	}

	// 공통 Error Response 생성
	private ResponseEntity<Map<String, Object>> buildError(
			HttpStatus status,
			String code,
			String message,
			String path
	) {
		return ResponseEntity.status(status).body(baseError(status, code, message, path));
	}

	// 표준 필드 조립
	private Map<String, Object> baseError(HttpStatus status, String code, String message, String path) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("timestamp", Instant.now().toString());
		body.put("status", status.value());
		body.put("code", code);
		body.put("message", message);
		body.put("path", path);
		return body;
	}
}
