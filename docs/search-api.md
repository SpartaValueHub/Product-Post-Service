# 헤더 검색 API (추천·연관)

Product-Post-Service 헤더 검색어 API 명세서입니다.  
일반 검색(상품 결과)은 기존 `GET /api/v1/product-posts?keyword=` 를 사용합니다.  
설계 배경: [search-architecture.md](./search-architecture.md)

## 공통

| 항목 | 내용 |
|------|------|
| Auth | 불필요 (FO 비로그인 허용). Gateway GET public |
| 원본 DB | 추천·연관 API는 판매글 테이블을 조회하지 않음 |
| Redis 장애 | 추천은 YAML 시드 fallback. 연관은 사전 후 popular fallback. 목록 검색은 DB로 정상 |

공통 응답:

```json
{
  "terms": ["롤렉스", "샤넬백"]
}
```

---

## GET /api/v1/search/popular

### Summary
추천(인기) 검색어 Top N을 반환한다.

### Method · Path
`GET /api/v1/search/popular`

### Auth
불필요

### Request
없음

### Response
`200 OK`

| 필드 | 타입 | 설명 |
|------|------|------|
| terms | string[] | 최대 `product-post.search.popular-limit`(기본 **5**). 스케줄이 구운 Redis LIST(`popular-serving-key`) 순. 비어 있으면 YAML `popular-seed` |


```json
{
  "terms": ["롤렉스", "샤넬백", "빈티지 백"]
}
```

### 서빙 방식

- Enter 검색 → 비동기 hour 버킷 `ZINCRBY` (`search:terms:h:{yyyyMMddHH}`) + (세션 있으면) 동시검색 카운터
- `@Scheduled` (기본 60분)가 24h/7d 가중 점수로 TopN을 `popular-serving-key` LIST에 스냅샷
- 가중: `count24h × weight-recent-24h + max(0, count7d − count24h) × weight-recent-7d-remainder` (YAML)
- 이 API는 스냅샷만 조회. 비어 있으면 `popular-seed`

### 동시검색 세션 (연관 베이크용)

목록 검색(`GET /product-posts?keyword=`) 시 선택 헤더:

| 헤더 | 설명 |
|------|------|
| `X-Member-Uuid` | Gateway가 넣는 로그인 회원 (검색자). 판매자 필터 `memberUuid` 쿼리와 별개 |
| `X-Search-Session-Id` | FE 발급 UUID 등. 비로그인 동시검색용 |

둘 다 없으면 인기 카운터만 쌓이고 동시검색은 기록하지 않는다.

### Errors
없음 (장애 시 시드 또는 빈 배열)

---

## GET /api/v1/search/related

### Summary
입력 쿼리에 대한 연관 검색어를 반환한다.

### Method · Path
`GET /api/v1/search/related`

### Auth
불필요

### Request

| 위치 | 필드 | 타입 | 필수 | 제약 |
|------|------|------|------|------|
| Query | q | string | Y | 검색어. blank면 빈 배열 |

### Response
`200 OK`

| 필드 | 타입 | 설명 |
|------|------|------|
| terms | string[] | 베이크된 Redis LIST(`search:related:{q}`) → 없으면 YAML 사전 → 없으면 추천 검색어. 자기 자신·중복 제외. 최대 `popular-limit` |


```json
{
  "terms": ["샤넬백 보증서", "샤넬 클래식"]
}
```

### Errors
없음 (blank `q` → `terms: []`)

---

## GET /api/v1/product-posts (일반 검색)

### Summary
헤더 Enter 후 상품 카드 목록. 기존 목록 API와 동일.

### Method · Path
`GET /api/v1/product-posts?keyword={q}`

### 동작 추가 (이번 작업)

- `keyword` 정규화: trim, 연속 공백 축약, 소문자, `keyword-max-length` truncate
- 정규화된 keyword가 있으면 **비동기**로 Redis hour 버킷 `ZINCRBY search:terms:h:{yyyyMMddHH}` (검색 응답과 무관, 실패 무시)
- 목록 쿼리: 제목 `MATCH ... AGAINST` (FULLTEXT ngram, `ft_pp_name_ngram`). 2글자 미만 keyword는 빈 목록. 인덱스 SQL: `scripts/add-product-post-name-fulltext.sql`

상세 Request/Response/Errors: [product-post-api.md](./product-post-api.md) 목록 섹션 참고.
