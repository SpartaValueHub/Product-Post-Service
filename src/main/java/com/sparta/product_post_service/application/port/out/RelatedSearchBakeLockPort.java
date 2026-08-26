package com.sparta.product_post_service.application.port.out;

// 연관 검색어 베이크 분산 락
public interface RelatedSearchBakeLockPort {

	boolean tryLock();

	void unlock();
}
