package com.sparta.product_post_service.application.port.out;

// 자동완성 사전 베이크 분산 락
public interface SuggestionDictionaryBakeLockPort {

	boolean tryLock();

	void unlock();
}
