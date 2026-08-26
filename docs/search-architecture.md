# 헤더 검색 아키텍처 (추천·연관·일반 검색)

> ES(유료 매니지드) 없이, DB 부하를 최소화하는 방향.  
> 가정: 회원 약 5천만 규모의 대규모 서비스.  
> 범위: 추천 검색어 / 연관 검색어 / 일반 검색(상품 결과).

---

## 1. 핵심 전제

회원 5천만 ≠ DB에 5천만 번 검색 쿼리.

위험한 것은 **타이핑·추천 UI 트래픽이 판매글(원본) 테이블을 직접 치는 것**이다.

검색을 세 층으로 분리한다.

| 층 | 기능 | 사용자 체감 | 금지 |
|----|------|-------------|------|
| A | 추천 검색어 | 검색창 비었을 때 “이거 어때?” | 판매글 전체 스캔·실시간 GROUP BY |
| B | 연관 검색어 | 입력/검색 후 “이것도?” | 요청 시점 무거운 JOIN·집계 |
| C | 일반 검색 | Enter → 실제 상품 목록 | `LIKE '%키워드%'` 전표 스캔 |

Elasticsearch는 좋은 검색 엔진이지 유일한 해법이 아니다.  
무료·저비용의 핵심은 **원본 DB와 검색 UI를 분리**하는 것이다.

---

## 2. 왜 MySQL만으로 헤더 검색을 하면 터지나

현재 목록 검색(개념, 개선 전):

```text
WHERE product_post_name LIKE '%빈티지%'
```

개선 후(제목 FULLTEXT ngram):

```text
WHERE MATCH(product_post_name) AGAINST ('빈티지' IN NATURAL LANGUAGE MODE)
```

- 앞쪽 `%` → 인덱스 사용 불가 → 데이터 증가 시 풀스캔에 가까워짐
- 헤더 자동완성·추천은 키 입력마다 호출 → QPS가 목록 검색보다 훨씬 큼
- 대규모 서비스에서는 마스터 DB CPU부터 고갈

따라서:

- **추천 / 연관 / (자동완성)** → 원본 판매글 테이블 조회 금지
- **일반 검색(결과)** → 원본(또는 FULLTEXT)은 쓰되, 인덱스가 타는 방식만 허용 + 읽기 분산

---

## 3. 전체 흐름

```text
[FE 헤더]
   │
   ├─ 빈 입력 ──────────────► GET /search/popular      ──► Redis (TopN)
   ├─ 타이핑 중(선택) ──────► GET /search/suggestions ──► Redis / 사전(prefix)
   ├─ 결과·사이드 ──────────► GET /search/related      ──► Redis (미리 계산된 맵)
   └─ Enter / 검색 버튼 ────► GET /product-posts?keyword= ──► DB(가능하면 리드) + 인덱스
                                      │
                                      ▼
                              (비동기) 검색 로그 적재
                                      │
                              배치가 점수·연관 계산 → Redis 갱신
```

**동기 경로**(사용자가 기다림)와 **비동기 경로**(로그·랭킹)를 나눈다.  
검색 API 응답이 로그 저장 완료를 기다리면 안 된다. (검색 성공 ≠ 로그 성공)

---

## 4. 추천 검색어 (Popular / Recommended)

### 정의

검색창이 비었거나 포커스만 왔을 때 보여주는 **인기·운영 추천 키워드** 목록.

### 왜 DB를 안 치나

추천은 **미리 뽑아 둔 리스트**면 충분하다.  
요청마다 `GROUP BY keyword ORDER BY COUNT` 하면 로그가 커질수록 DB가 죽는다.

### 저부하 방식 (권장)

1. 사용자가 검색(Enter)할 때 검색어를 **비동기 로그/카운터**로만 남긴다.
2. **배치(예: 5~15분)** 가 점수를 계산해 Top 10~20을 만든다.
3. 결과를 **Redis 한 키**에 저장한다. (`search:popular` 등)
4. API는 Redis만 읽는다. → 서빙 경로에서 MySQL 거의 0회.

### 점수 정책 예시

```text
score = 최근 24시간 검색수 × 3
      + 최근 7일 검색수 × 1
      + 검색 후 목록/상세 클릭 × 2
```

부가 규칙:

- 최소 검색수 컷 (콜드·노이즈 제거)
- 금칙어 필터
- 동의어·표기 통일 (`빈티지백` / `빈티지 백`)
- 초기에 로그가 없으면 **운영 시드 키워드**(카테고리명, 시즌)로 bootstrap
- (선택) 운영 pin 2~3개 + 나머지 자동 TopN

### 설명용 한 줄

> 추천 검색어는 실시간 집계가 아니라, 비동기 로그를 배치가 집계한 TopN을 Redis에서 서빙한다.

---

## 5. 연관 검색어 (Related)

### 정의

이미 어떤 쿼리 `q`가 있을 때, **옆길로 유도**하는 키워드.

### 왜 실시간 DB 조인이 위험한가

“A 다음에 뭘 많이 검색했지?”를 요청 순간 SQL로 하면  
로그 증가와 함께 무거운 셀프조인·집계가 된다.

### 저부하 방식 (단계)

**1단계 (데이터 적어도 가능) — 사전·카테고리**

- `샤넬백` → 같은 카테고리 인기 검색어, 사전 확장  
  예: `샤넬백 보증서`, `샤넬 클래식`
- `q → [관련…]` 매핑을 **미리** 만들어 Redis에 캐시

**2단계 (로그 축적 후) — 동시 검색 (co-occurrence)**

- 같은 세션/유저가 A 다음 B를 검색한 횟수를 **배치로** 집계
- `related:{정규화된q}` = `[…]` 를 Redis에 저장
- API는 키 조회만

**안전장치**

- 관련도 부족 시 **인기 검색어 fallback**
- 자기 자신·동의어 제외
- 1차에서는 유저별 개인화 제외 (캐시 키 폭발·복잡도 방지)

### 설명용 한 줄

> 연관어는 요청 시점에 계산하지 않고, 카테고리/동시검색을 배치로 구워 Redis 맵으로 제공한다.

---

## 6. 일반 검색 (상품 결과)

### 정의

Enter 이후 **판매글 카드 목록**.  
현재 ValueHub: `GET /api/v1/product-posts?keyword=...`

### ES 없이 DB 부하를 줄이는 방법

| 방식 | 부하 | 품질 | 비고 |
|------|------|------|------|
| `LIKE '%단어%'` | 최악 | 보통 | 대규모 금지에 가깝다 |
| `LIKE '단어%'` (prefix) | 양호 | 앞글자만 | 제목 prefix용 |
| MySQL InnoDB **FULLTEXT** | 중~양호 | 단어 단위 | 매니지드 ES 없이도 가능 |
| 검색용 요약 테이블 | 양호 | 설계 따름 | 제목·카테고리 등만 복제 |

추가 원칙:

1. **읽기 분산** — 검색/목록은 리드 레플리카, 마스터는 쓰기 위주
2. **검색 대상 축소** — `PUBLIC`·미삭제·(가능하면) 최근 N일 등
3. **짧은 결과 캐시** — 동일 keyword+필터+page (개인화·실시간성과 트레이드오프)
4. 그래도 부족할 때 — OpenSearch **셀프호스트** 등 검토 (유료 매니지드 ES와는 다른 축)

회원 5천만과 별개로, 병목은 보통 **활성 공개 판매글 수 × 검색 QPS** 이다.

### 설명용 한 줄

> 일반 검색만 원본(또는 FULLTEXT)을 치고, UI용 추천/연관은 원본을 치지 않는다. 원본 쿼리는 인덱스가 타는 형태만 허용한다.

---

## 7. 데이터 흐름 요약

```text
[사용자 검색 Enter]
        │
        ├─► 목록 API (동기) ──► DB / FULLTEXT
        │
        └─► 검색 로그 (비동기, 실패해도 검색은 성공)
                │
                ▼
         로그·카운터 (Redis INCR 및/또는 로그 테이블 append)
                │ 배치
                ▼
         인기 TopN  → 추천 검색어 API
         연관 맵    → 연관 검색어 API
         사전       → (선택) 자동완성
                │
                ▼
              Redis 서빙
```

### Redis를 쓰는 이유

| | MySQL만으로 TopN 서빙 | Redis TopN 서빙 |
|--|----------------------|-----------------|
| 매 요청 | 로그 커지면 GROUP BY 위험 | 읽기 비용 낮음 |
| 헤더 QPS | DB가 그대로 노출 | DB 보호 |
| 역할 | 원본·장기 보관에 적합 | 캐시·랭킹 서빙에 적합 |

원본 진실(로그)은 DB에 둘 수 있어도, **핫 서빙은 Redis**가 맞다.

---

## 8. 어느 서비스에서 작업하나?

### 결론: Category-Service 아님. **1차는 Product-Post-Service**

| 후보 | 적합? | 이유 |
|------|-------|------|
| **Category-Service** | ❌ | 카테고리 마스터 도메인. 검색어·판매글 검색 책임과 무관 |
| **Product-Post-Service** | ✅ 1차 | 이미 `GET /product-posts?keyword=` 일반 검색이 여기 있음. 검색 로그·인기/연관도 “상품 검색”에 붙는 것이 자연스러움 |
| Search-Service (신규) | △ 이후 | 검색이 커지고 Auth/타 도메인 검색까지 합치면 분리 검토 |

### 왜 Category가 아닌가

- 연관 검색에 **카테고리 UUID/이름**을 참고할 수는 있다.
- 그렇다고 검색 API·검색 로그·인기 랭킹을 Category가 소유하면 안 된다.  
  (Category는 “분류”, Search/Listing은 “찾기”)

### 1차 구현 위치 (권장)

```text
Product-Post-Service
  ├─ GET /api/v1/product-posts?keyword=     (기존 일반 검색 — 이후 FULLTEXT 등 개선)
  ├─ GET /api/v1/search/popular             (추천 검색어)
  ├─ GET /api/v1/search/related?q=          (연관 검색어)
  ├─ GET /api/v1/search/suggestions?q=      (자동완성)
  └─ (내부) 검색 로그 적재 + 배치/스케줄로 Redis 갱신
```

Gateway 라우팅만 Product-Post로 추가하면 된다.  
Category가 필요하면 Product-Post가 Category를 **조회·참고**할 뿐, API 소유는 Product-Post.

### Redis

현재 Redis는 Auth/Gateway 쪽 보안·세션에 쓰인다.  
검색용은 동일 Redis에 **key prefix `search:`** 로 분리한다.

| 키 | 용도 |
|----|------|
| `search:terms:h:{yyyyMMddHH}` | 시간 버킷 검색어 ZSET (`ZINCRBY`, TTL ~8일) |
| `search:terms:agg:{uuid}` | 베이크 시 ZUNIONSTORE 임시 키 |
| `search:terms:z` | (레거시) 전체 누적 ZSET — 가중 점수 도입 후 미사용 |
| `search:popular` | 추천 TopN 서빙 LIST (`@Scheduled` 베이크 스냅샷) |
| `search:popular:bake-lock` | 다중 인스턴스 추천 베이크 분산 락 |
| `search:cooc:z:{q}` | q 다음 검색어 동시검색 ZSET |
| `search:cooc:sources` | 동시검색이 발생한 from 검색어 SET |
| `search:related:{q}` | 연관 TopN 서빙 LIST |
| `search:related:bake-lock` | 연관 베이크 분산 락 |
| `search:session:last:{sessionKey}` | 세션 직전 검색어 |
| `search:suggest:dict` | 자동완성 lex ZSET (`ZRANGEBYLEX`, 베이크 스냅샷) |
| `search:suggest:bake-lock` | 자동완성 사전 베이크 분산 락 |

연관: 베이크 LIST → YAML 사전 → popular fallback. Redis 연관 맵은 동시검색 베이크로 채움.

---

## 9. 구현 순서 (합의안)

1. **검색 로그(비동기)** + **추천 검색어 API** (`/search/popular`) — 완료
2. **연관 검색어 API** (`/search/related`) — 1차는 YAML 사전, 이후 동시검색 — 완료
3. **일반 검색 연결** — keyword 정규화 + 비동기 Redis 카운터 — 완료
4. **일반 검색 쿼리 개선** — `LIKE %…%` → MySQL FULLTEXT(ngram, 제목만) — 완료
5. **추천 TopN 주기 베이크** — `@Scheduled` → `search:popular` 서빙 분리 — 완료
5-b. **추천 가중 점수(24h/7d)** — 시간 버킷 + 가중 베이크 — 완료
6. **연관 동시검색 베이크** — 세션 A→B 카운터 → `search:related:{q}` — 완료
7. **자동완성** `suggestions` — Redis lex 사전 + YAML fallback — 진행
8. (선택) 최근 검색어(유저별 Redis)

### 추천 TopN 베이크 요약

- 기술: `@Scheduled` (Spring Batch 프레임워크 아님). 주기·키·최소점수는 YAML
- 기본: `popular-limit=5`, `bake-interval-ms=3600000`(60분)
- Enter → 현재 시각 hour 버킷 `ZINCRBY` / 스케줄 → 24h·7d 집계 → 가중 점수 → TopN → `popular-serving-key` LIST
- 가중: `score = count24h × weight-recent-24h + max(0, count7d − count24h) × weight-recent-7d-remainder`
- 후보 상한: 윈도우별 `bake-candidate-limit`(기본 500) — 전체 멤버 스캔 금지
- API는 서빙 LIST만 읽음. 비면 YAML seed
- 다중 인스턴스: Redis `SET NX` 락

### FULLTEXT(ngram) 적용 요약

- 대상: `product_post.product_post_name` 만
- 인덱스: `ft_pp_name_ngram` (`scripts/add-product-post-name-fulltext.sql`, ddl-auto로는 생성 안 됨)
- 모드: `NATURAL LANGUAGE MODE`
- keyword 없음 → 기존 JPQL 목록 / keyword 있음 → native `MATCH`
- 2글자 미만 → LIKE 폴백 없이 빈 결과 (ngram_token_size=2)
- 최소 길이는 `product-post.search.fulltext-min-keyword-length` 설정 (하드코딩 금지)

### 아직 미룬 것

- 클릭 가중, 금칙어 필터, Category HTTP 연동
- 최근 검색어·운영 pin Admin
- 제목+본문 FULLTEXT, 리드 레플리카
- 자동완성 인기순 정렬(현재 lex 사전순)


---

## 10. 구현 전 고정하면 좋은 결정

1. 추천: Top 몇 개? 갱신 주기? 운영 pin 여부?
2. 연관: 1차 범위 = 카테고리/사전만? 동시검색까지?
3. 일반 검색 FULLTEXT: 같은 스프린트? 다음?
4. 검색 로그 저장소: Redis 카운터만? MySQL 로그 테이블?
5. 실패 시: popular/related 빈 배열 vs seed fallback?

---

## 11. 팀 설명용 초단문

- **추천**: 배치 TopN → Redis 서빙. 원본 판매글 안 침.
- **연관**: 미리 구운 맵 → Redis 서빙. 요청 시 집계 안 함.
- **일반 검색**: Enter 때만 DB. 인덱스가 타는 쿼리만. UI 트래픽과 분리.
- **작업 서비스**: Category 아님. **Product-Post-Service** (기존 keyword 검색과 동일 바운디드 컨텍스트).
