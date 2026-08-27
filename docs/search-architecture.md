# 헤더 검색 아키텍처 (추천·연관·일반 검색)

> ES(유료 매니지드) 없이, DB 부하를 최소화하는 방향.  
> 가정: 회원 약 5천만 규모의 대규모 서비스.  
> 범위: 추천 검색어 / 연관 검색어 / 일반 검색(상품 결과). (자동완성은 선택 구현 완료)  
> API 명세: [search-api.md](./search-api.md)  
> 작업 서비스: **Product-Post-Service** (Category-Service 아님)

---

## 0. 붙여넣기 초안 대비 변경 요약

### 삭제·폐기
- `LIKE '%…%'` 를 현재 일반 검색 방식으로 서술 → **폐기** (FULLTEXT ngram으로 전환 완료)
- “배치 5~15분”만 강조한 미정 수치 → **실제 YAML 기본 60분**으로 교체
- “검색 로그 MySQL 테이블 append”를 1차 필수처럼 서술 → **1차는 Redis 카운터만** (MySQL 로그 테이블 미도입)
- Category가 검색 API를 소유한다는 뉘앙스 → **삭제**
- 구현 전 미결 질문(§10 구버전) → **결정 완료 표로 교체**
- 최근 검색어(유저별 Redis) → **이번 범위에서 스킵** (필요 시 후속)

### 수정
- 추천 점수: 클릭×2 포함 예시 → **클릭 가중은 미구현**. 실제는 `24h×3 + (7d−24h)×1`
- 연관 1단계 “카테고리 인기” → 실제 1차는 **YAML 사전** (+ popular fallback). Category HTTP 연동은 후속
- 연관 2단계 동시검색 → **구현 완료** (`X-Member-Uuid` / `X-Search-Session-Id`)
- 일반 검색 → **제목 FULLTEXT(ngram) 적용 완료**
- Redis → Auth/Gateway와 **동일 Redis**, prefix `search:` 분리
- 구현 순서 → 전부 **필수 완료**. 최근 검색어만 선택 미착수

### 추가
- hour 버킷 카운터 `search:terms:h:{yyyyMMddHH}`
- 가중 추천 베이크·후보 상한(`bake-candidate-limit`)
- 자동완성 `GET /search/suggestions` (선택, 구현 완료)
- Redis 키 표·Gateway public `/*/api/v1/search/**`
- FE 연동 매핑 표

---

## 1. 핵심 전제

회원 5천만 ≠ DB에 5천만 번 검색 쿼리.

위험한 것은 **타이핑·추천 UI 트래픽이 판매글(원본) 테이블을 직접 치는 것**이다.

검색을 세 층(+선택 자동완성)으로 분리한다.

| 층 | 기능 | 사용자 체감 | 금지 |
|----|------|-------------|------|
| A | 추천 검색어 | 검색창 비었을 때 “이거 어때?” | 판매글 전체 스캔·실시간 GROUP BY |
| B | 연관 검색어 | 입력/검색 후 “이것도?” | 요청 시점 무거운 JOIN·집계 |
| C | 일반 검색 | Enter → 실제 상품 목록 | `LIKE '%키워드%'` 전표 스캔 |
| (선택) | 자동완성 | 타이핑 중 prefix 후보 | 판매글 테이블 prefix 조회 |

Elasticsearch는 좋은 검색 엔진이지 유일한 해법이 아니다.  
무료·저비용의 핵심은 **원본 DB와 검색 UI를 분리**하는 것이다.

---

## 2. 왜 MySQL만으로 헤더 검색을 하면 터지나

개선 전(개념):

```text
WHERE product_post_name LIKE '%빈티지%'
```

개선 후(현재, 제목 FULLTEXT ngram):

```text
WHERE MATCH(product_post_name) AGAINST ('빈티지' IN NATURAL LANGUAGE MODE)
```

- 앞쪽 `%` → 인덱스 사용 불가 → 데이터 증가 시 풀스캔에 가까워짐
- 헤더 자동완성·추천은 키 입력마다 호출 → QPS가 목록 검색보다 훨씬 큼
- 대규모 서비스에서는 마스터 DB CPU부터 고갈

따라서:

- **추천 / 연관 / 자동완성** → 원본 판매글 테이블 조회 금지
- **일반 검색(결과)** → FULLTEXT(인덱스)만 허용. (리드 레플리카는 후속)

---

## 3. 전체 흐름

```text
[FE 헤더]
   │
   ├─ 빈 입력 ──────────────► GET /api/v1/search/popular         ──► Redis LIST (TopN)
   ├─ 타이핑 중(선택) ──────► GET /api/v1/search/suggestions?q= ──► Redis lex 사전 / YAML fallback
   ├─ 결과·사이드 ──────────► GET /api/v1/search/related?q=     ──► Redis LIST → YAML → popular
   └─ Enter / 검색 버튼 ────► GET /api/v1/product-posts?keyword= ──► MySQL FULLTEXT(ngram)
                                      │
                                      ▼
                              (비동기) hour 버킷 ZINCRBY + (세션 있으면) 동시검색
                                      │
                              @Scheduled 베이크 → popular / related / suggest 갱신
```

**동기 경로**(사용자가 기다림)와 **비동기 경로**(카운터·베이크)를 나눈다.  
검색 API 응답이 로그/카운터 저장 완료를 기다리면 안 된다. (검색 성공 ≠ 카운터 성공)

---

## 4. 추천 검색어 (Popular)

### 정의

검색창이 비었거나 포커스만 왔을 때 보여주는 **인기·운영 추천 키워드** 목록.

### 현재 구현

1. Enter 검색 시 정규화 키워드를 **비동기**로 hour 버킷에 `ZINCRBY`  
   (`search:terms:h:{yyyyMMddHH}`, TTL ~8일)
2. `@Scheduled`(기본 **60분**)가 24h/7d 가중 점수로 TopN 계산
3. 결과를 **`search:popular` LIST**에 스냅샷
4. `GET /search/popular`는 스냅샷만 조회. 비면 YAML `popular-seed`

### 점수 (현재)

```text
score = count24h × weight-recent-24h(기본 3)
      + max(0, count7d − count24h) × weight-recent-7d-remainder(기본 1)
```

- 클릭 가중: **미구현** (후속)
- 후보 상한: 윈도우별 `bake-candidate-limit`(기본 500)
- TopN 개수: `popular-limit`(기본 **5**)
- 금칙어·운영 pin·동의어 사전: **미구현** (정규화는 trim/공백축약/소문자/길이제한)

### 설명용 한 줄

> 추천 검색어는 실시간 집계가 아니라, 비동기 hour 카운터를 스케줄이 가중 집계한 TopN을 Redis에서 서빙한다.

---

## 5. 연관 검색어 (Related)

### 정의

이미 어떤 쿼리 `q`가 있을 때, **옆길로 유도**하는 키워드.

### 현재 구현 (1단계 + 2단계 모두)

**조회 우선순위**

1. Redis `search:related:{정규화q}` (동시검색 베이크 LIST)
2. YAML `product-post.search.related` 사전
3. 추천 검색어(popular) fallback  
자기 자신·중복 제외. 최대 `popular-limit`.

**동시검색 기록** (목록 검색 Enter 시, 선택 헤더)

| 헤더 | 용도 |
|------|------|
| `X-Member-Uuid` | 로그인 검색자 (Gateway) |
| `X-Search-Session-Id` | 비로그인 FE 세션 |

둘 다 없으면 인기 카운터만 쌓이고 동시검색은 기록하지 않는다.  
스케줄이 `search:cooc:z:{q}` → `search:related:{q}` 로 베이크.

### 설명용 한 줄

> 연관어는 요청 시점에 계산하지 않고, 동시검색(및 YAML 사전)을 미리 구워 Redis/설정으로 제공한다.

---

## 6. 일반 검색 (상품 결과)

### 정의

Enter 이후 **판매글 카드 목록**.  
`GET /api/v1/product-posts?keyword=...`

### 현재

| 방식 | 상태 |
|------|------|
| `LIKE '%단어%'` | ❌ 제거됨 |
| MySQL FULLTEXT **ngram** (제목만) | ✅ `ft_pp_name_ngram` |
| keyword 정규화 + 비동기 Redis 카운터 | ✅ |
| 리드 레플리카 / 결과 캐시 | ⬜ 후속 |
| 제목+본문 FULLTEXT | ⬜ 후속 |

- 2글자 미만 keyword → 빈 목록 (`fulltext-min-keyword-length`, ngram_token_size=2)
- 인덱스 SQL: `scripts/add-product-post-name-fulltext.sql` (ddl-auto로 생성 안 됨)

### 설명용 한 줄

> 일반 검색만 원본 FULLTEXT를 치고, UI용 추천/연관/자동완성은 원본을 치지 않는다.

---

## 7. 데이터 흐름 요약

```text
[사용자 검색 Enter]
        │
        ├─► 목록 API (동기) ──► MySQL FULLTEXT(ngram, 제목)
        │
        └─► 카운터 (비동기, 실패해도 검색은 성공)
                │
                ▼
         hour ZSET + (세션 있으면) co-occurrence
                │ @Scheduled (~60분)
                ▼
         popular LIST / related LIST / suggest dict
                │
                ▼
              Redis 서빙 (API는 읽기만)
```

### Redis를 쓰는 이유

| | MySQL만으로 TopN 서빙 | Redis TopN 서빙 |
|--|----------------------|-----------------|
| 매 요청 | 로그 커지면 GROUP BY 위험 | 읽기 비용 낮음 |
| 헤더 QPS | DB가 그대로 노출 | DB 보호 |
| 역할 | 원본·장기 보관에 적합 | 캐시·랭킹 서빙에 적합 |

1차는 **Redis 카운터 + 베이크 서빙**만 사용. MySQL 검색 로그 테이블은 없음.

---

## 8. 어느 서비스에서 작업하나?

### 결론: Category-Service 아님. **Product-Post-Service**

| 후보 | 적합? | 이유 |
|------|-------|------|
| **Category-Service** | ❌ | 카테고리 마스터. 검색어·판매글 검색 책임과 무관 |
| **Product-Post-Service** | ✅ | `keyword` 목록 검색·검색 카운터·popular/related/suggestions 모두 여기 |
| Search-Service (신규) | △ 이후 | 검색이 커지면 분리 검토 |

### API 위치 (현재)

```text
Product-Post-Service
  ├─ GET /api/v1/product-posts?keyword=     (일반 검색 — FULLTEXT)
  ├─ GET /api/v1/search/popular             (추천)
  ├─ GET /api/v1/search/related?q=          (연관)
  ├─ GET /api/v1/search/suggestions?q=      (자동완성, 선택)
  └─ (내부) 비동기 카운터 + @Scheduled Redis 베이크
```

Gateway: `/*/api/v1/search/**` **public** (Auth 불필요).  
동일 Apps Redis, key prefix `search:`.

### Redis 키

| 키 | 용도 |
|----|------|
| `search:terms:h:{yyyyMMddHH}` | 시간 버킷 검색어 ZSET (`ZINCRBY`, TTL ~8일) |
| `search:terms:agg:{uuid}` | 베이크 시 ZUNIONSTORE 임시 키 |
| `search:terms:z` | (레거시) 미사용 |
| `search:popular` | 추천 TopN 서빙 LIST |
| `search:popular:bake-lock` | 추천 베이크 락 |
| `search:cooc:z:{q}` | 동시검색 ZSET |
| `search:cooc:sources` | 동시검색 from 검색어 SET |
| `search:related:{q}` | 연관 TopN 서빙 LIST |
| `search:related:bake-lock` | 연관 베이크 락 |
| `search:session:last:{sessionKey}` | 세션 직전 검색어 |
| `search:suggest:dict` | 자동완성 lex ZSET |
| `search:suggest:bake-lock` | 자동완성 사전 베이크 락 |

---

## 9. 구현 상태

| # | 항목 | 상태 |
|---|------|------|
| 1 | 검색 로그(비동기) + `GET /search/popular` | ✅ |
| 2 | `GET /search/related` (YAML + popular fallback) | ✅ |
| 3 | keyword 정규화 + Redis 카운터 + Gateway/Infra Redis | ✅ |
| 4 | FULLTEXT(ngram, 제목) | ✅ |
| 5 | 추천 TopN 스케줄 베이크 → `search:popular` | ✅ |
| 5-b | 가중 점수(24h/7d) + hour 버킷 | ✅ |
| 6 | 동시검색 베이크 → `search:related:{q}` | ✅ |
| 7 | (선택) `GET /search/suggestions` | ✅ |
| 8 | (선택) 최근 검색어(유저별 Redis) | ⬜ **스킵** (FE 추천·연관만 사용 시 불필요) |

### 후속(미룬 것)

- 클릭 가중, 금칙어, 운영 pin Admin
- Category HTTP 연동, 제목+본문 FULLTEXT, 리드 레플리카
- 자동완성 인기순 정렬(현재 lex 사전순)
- 최근 검색어

---

## 10. 결정 완료 사항 (구 §10 미결 대체)

| 항목 | 결정 |
|------|------|
| 추천 TopN | 기본 **5**, 주기 **60분**, pin 없음 |
| 연관 | YAML 사전 + 동시검색 베이크 + popular fallback |
| FULLTEXT | 제목 ngram, 같은 서비스에서 적용 완료 |
| 로그 저장소 | Redis hour 카운터만 (MySQL 로그 테이블 없음) |
| 실패 시 | popular → seed / related → YAML → popular / suggestions → seed·related prefix |
| 최근 검색어 | 이번 범위 **안 함** |

---

## 11. FE 연동 매핑

명세: [search-api.md](./search-api.md)  
공통 응답: `{ "terms": ["…"] }` · Auth 불필요

| 화면/시점 | API |
|-----------|-----|
| 검색창 비었을 때 | `GET /api/v1/search/popular` |
| 검색 후·사이드 연관 | `GET /api/v1/search/related?q=` |
| (선택) 타이핑 자동완성 | `GET /api/v1/search/suggestions?q=` (**2글자 이상**) |
| Enter 상품 목록 | `GET /api/v1/product-posts?keyword=` |

목록 검색 시 연관 품질용(선택): `X-Member-Uuid` 또는 `X-Search-Session-Id`

---

## 12. 팀 설명용 초단문

- **추천**: 배치(가중) TopN → Redis 서빙. 원본 판매글 안 침.
- **연관**: 베이크/YAML 맵 → Redis·설정 서빙. 요청 시 집계 안 함.
- **일반 검색**: Enter 때만 DB FULLTEXT. UI 트래픽과 분리.
- **자동완성(선택)**: Redis lex 사전. DB 안 침.
- **작업 서비스**: Category 아님. **Product-Post-Service**.
- **최근 검색어**: 이번 범위 제외.
