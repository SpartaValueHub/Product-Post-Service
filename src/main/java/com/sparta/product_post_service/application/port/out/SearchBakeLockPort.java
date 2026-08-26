package com.sparta.product_post_service.application.port.out;

// 추천 TopN 베이크 분산 락 (다중 인스턴스 중복 실행 방지)
public interface SearchBakeLockPort {

	// 락 획득 성공 여부
	boolean tryLock();

	// 락 해제 (TTL 만료 대비로 실패해도 무시 가능)
	void unlock();
}
