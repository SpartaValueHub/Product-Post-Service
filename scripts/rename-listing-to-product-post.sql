-- listing → product_post 스키마/테이블/컬럼 이름 변경
-- 팀 MySQL에서 DBA/담당자가 실행. 실행 전 백업 권장.
-- 로컬에서 데이터가 없어도 되면: product_post_db만 생성 후 앱 ddl-auto=update로 신규 생성해도 됩니다.

CREATE DATABASE IF NOT EXISTS product_post_db
	DEFAULT CHARACTER SET utf8mb4
	DEFAULT COLLATE utf8mb4_unicode_ci;

-- 1) 기존 listing_db 테이블/컬럼 rename (데이터가 있을 때)
USE listing_db;

RENAME TABLE
	listing TO product_post,
	listing_image TO product_post_image,
	listing_document TO product_post_document;

ALTER TABLE product_post
	CHANGE COLUMN listing_id product_post_id BIGINT NOT NULL AUTO_INCREMENT,
	CHANGE COLUMN listing_uuid product_post_uuid VARCHAR(36) NOT NULL,
	CHANGE COLUMN listing_name product_post_name VARCHAR(100) NOT NULL,
	CHANGE COLUMN listing_status product_post_status VARCHAR(20) NOT NULL;

ALTER TABLE product_post_image
	CHANGE COLUMN listing_image_id product_post_image_id BIGINT NOT NULL AUTO_INCREMENT,
	CHANGE COLUMN listing_id product_post_id BIGINT NOT NULL,
	CHANGE COLUMN listing_image_uuid product_post_image_uuid VARCHAR(36) NOT NULL;

ALTER TABLE product_post_document
	CHANGE COLUMN listing_document_id product_post_document_id BIGINT NOT NULL AUTO_INCREMENT,
	CHANGE COLUMN listing_id product_post_id BIGINT NOT NULL,
	CHANGE COLUMN listing_document_uuid product_post_document_uuid VARCHAR(36) NOT NULL;

-- 2) product_post_db로 테이블 이동
RENAME TABLE
	listing_db.product_post TO product_post_db.product_post,
	listing_db.product_post_image TO product_post_db.product_post_image,
	listing_db.product_post_document TO product_post_db.product_post_document,
	listing_db.bump_quota TO product_post_db.bump_quota;

-- 3) 앱 .env / SPRING_DATASOURCE_URL 의 스키마를 product_post_db 로 변경 후 기동
