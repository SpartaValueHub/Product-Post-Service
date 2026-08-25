-- 판매글 목록 tradeStatus·memberUuid 필터용 복합 인덱스
-- Hibernate ddl-auto=update 환경에서도 Entity @Index와 동일 목적. prod validate 시 수동 적용.

CREATE INDEX idx_pp_member_trade_list
	ON product_post (member_uuid, trade_status, product_post_status, deleted_at, bumped_at, created_at);

CREATE INDEX idx_pp_trade_public_list
	ON product_post (trade_status, product_post_status, deleted_at, bumped_at, created_at);
