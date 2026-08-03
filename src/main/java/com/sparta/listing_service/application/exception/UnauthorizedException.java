package com.sparta.listing_service.application.exception;

public class UnauthorizedException extends RuntimeException {

	public UnauthorizedException(String message) {
		super(message);
	}

}
