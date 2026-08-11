package com.sparta.product_post_service.adaptor.out.mysql.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.product_post_service.adaptor.out.mysql.entity.BumpQuotaEntity;

// 끌올 일일 횟수 JPA Repository
public interface BumpQuotaJpaRepository extends JpaRepository<BumpQuotaEntity, Long> {

	// 회원 UUID + 기준일로 조회 (UNIQUE)
	Optional<BumpQuotaEntity> findByMemberUuidAndQuotaDate(String memberUuid, LocalDate quotaDate);
}
