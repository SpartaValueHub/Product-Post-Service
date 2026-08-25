-- 판매글 목록 카드용 거래 희망 동·구 컬럼
-- Hibernate ddl-auto=update 환경에서도 Entity와 동일 목적. prod validate 시 수동 적용.

ALTER TABLE product_post
	ADD COLUMN region_dong VARCHAR(50) NULL COMMENT '거래 희망 동(읍면동)' AFTER place_name;

ALTER TABLE product_post
	ADD COLUMN region_gu VARCHAR(50) NULL COMMENT '거래 희망 구(시군구)' AFTER region_dong;
