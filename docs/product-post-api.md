# ProductPost API (FE 연동 스펙)

Product-Post-Service 판매글(ProductPost) API 명세서입니다.  
식별자는 내부 PK가 아니라 **`productPostUuid`** 를 사용합니다.

---

## 개요

| 항목 | 내용 |
|------|------|
| Base Path | `/api/v1/product-posts` |
| Auth | Gateway 경유 시 JWT 검증 후 `X-Member-Uuid` 전달. 판매자 UUID는 헤더로 식별 |
| Content-Type | `application/json` |
| 문자 인코딩 | UTF-8 |

### 공통 Error Response

```json
{
  "timestamp": "2026-08-10T01:00:00.000Z",
  "status": 400,
  "code": "VALIDATION_FAILED",
  "message": "요청 값이 올바르지 않습니다.",
  "path": "/api/v1/product-posts",
  "fieldErrors": [
    { "field": "productPostName", "code": "NotBlank", "message": "상품명은 필수입니다." }
  ]
}
```

| status | code | 의미 |
|--------|------|------|
| 400 | VALIDATION_FAILED | Bean Validation 실패 (`fieldErrors` 포함) |
| 400 | INVALID_ARGUMENT | Domain/형식 오류, JSON 파싱 실패 |
| 401 | UNAUTHORIZED | `X-Member-Uuid` 없음·공백 |
| 403 | FORBIDDEN | 판매자 본인이 아님 |
| 404 | PRODUCT_POST_NOT_FOUND | 판매글 없음, 또는 숨김·삭제되어 미노출 |
| 400 | INVALID_CONTENT_TYPE | Presigned: 허용되지 않는 Content-Type |
| 400 | INVALID_CONTENT_LENGTH | Presigned: 용량 범위 초과·누락 |
| 400 | INVALID_MEDIA_KEY | 미디어 URL/key 형식 오류 |
| 400 | MEDIA_OBJECT_NOT_FOUND | pending 객체가 S3에 없음 |
| 500 | MEDIA_CONFIG_MISSING | Presigned: S3_BUCKET / CLOUDFRONT_BASE_URL 미설정 |
| 500 | MEDIA_STORAGE_FAILED | S3 Copy·Delete 실패 |

---

## POST /api/v1/product-posts/media/presigned-url

### Summary
판매글·증빙 이미지 업로드용 S3 Presigned PUT URL과 CloudFront `publicUrl`을 발급한다.  
발급 key는 **미확정 `pending/posts/{memberUuid}/...`** 이다. PUT 후 등록/수정 API의 `images[].imageUrl` / `documents[].imageUrl`에 받은 `publicUrl`을 넣으면 BE가 정식 `posts/` 로 승격한다.

### Method · Path
`POST /api/v1/product-posts/media/presigned-url`

### Auth
필요. Gateway가 넣는 헤더:

| Header | 필수 | 설명 |
|--------|------|------|
| `X-Member-Uuid` | Y | 판매자 회원 UUID |

### Request (Body)

| 필드 | 타입 | 필수 | 제약 |
|------|------|------|------|
| contentType | string | Y | `image/jpeg` · `image/png` · `image/webp` · `image/gif` |
| contentLength | number | Y | 1 이상 5,242,880(5MB) 이하 |

```json
{
  "contentType": "image/jpeg",
  "contentLength": 1048576
}
```

### Response (200)

| 필드 | 타입 | 설명 |
|------|------|------|
| uploadUrl | string | S3 Presigned PUT URL |
| s3Key | string | `pending/posts/{memberUuid}/{uuid}.{ext}` |
| publicUrl | string | `CLOUDFRONT_BASE_URL` + `/` + s3Key |
| expiresInSeconds | number | 기본 300 |

### 클라이언트 업로드

1. 본 API로 `uploadUrl`·`publicUrl` 수신 (`s3Key`는 `pending/posts/...`)
2. `uploadUrl`로 **PUT** (헤더 `Content-Type`은 요청과 **동일**)
3. `POST/PUT /product-posts`의 `imageUrl`에 Presign `publicUrl` 전달 → BE가 `posts/`로 승격 후 DB 저장

### Errors

| status | code | 의미 |
|--------|------|------|
| 400 | INVALID_CONTENT_TYPE | 허용되지 않는 Content-Type |
| 400 | INVALID_CONTENT_LENGTH | 용량 범위 초과·누락 |
| 401 | UNAUTHORIZED | `X-Member-Uuid` 없음·공백 |
| 500 | MEDIA_CONFIG_MISSING | S3_BUCKET / CLOUDFRONT_BASE_URL 미설정 |

---

## POST /api/v1/product-posts

### Summary
판매글을 등록한다. (FO)

### Method · Path
`POST /api/v1/product-posts`

### Auth
필요. Gateway가 넣는 헤더:

| Header | 필수 | 설명 |
|--------|------|------|
| `X-Member-Uuid` | Y | 판매자 회원 UUID (Body에 넣지 않음) |

로컬에서 ProductPost만 직접 호출할 때는 Swagger/Postman에 동일 헤더를 수동으로 넣는다.

### Request

| 필드 | 타입 | 필수 | 제약 |
|------|------|------|------|
| categoryUuid | string | Y | 리프 카테고리 UUID (원격 검증은 후속) |
| productPostName | string | Y | 2~100자 |
| conditionGrade | string | Y | S / A / B / C |
| price | number | Y | `product-post.policy.min-price` 이상 (기본 500000, 환경변수 `PRODUCT_POST_MIN_PRICE`로 변경 가능) |
| description | string | Y | 최대 2000자 |
| latitude | number | Y | 위도 |
| longitude | number | Y | 경도 |
| placeName | string | Y | 최대 100자 |
| regionDong | string | N | 거래 희망 동(읍면동). 최대 50자. FE 표시 1순위 |
| regionGu | string | N | 거래 희망 구(시군구). 최대 50자. FE 표시 2순위 (`regionDong` 없을 때) |
| images | array | Y | 1~10개. **배열 순서 = 노출 순서**(0번 인덱스가 대표/썸네일). 서버가 `sort_order` 1..n 부여 |
| images[].imageUrl | string | Y | 최대 500자. Presign `publicUrl`(pending) 또는 이미 승격된 `posts/` CloudFront URL. 저장 값은 정식 URL |
| documents | array | N | 선택 |
| documents[].documentType | string | Y(항목 시) | `WARRANTY` \| `RECEIPT` \| `APPRAISAL` |
| documents[].imageUrl | string | Y(항목 시) | 최대 500자 |

```json
{
  "categoryUuid": "11111111-1111-1111-1111-111111111111",
  "productPostName": "빈티지 백",
  "conditionGrade": "A",
  "price": 550000,
  "description": "상태 좋은 빈티지 백입니다.",
  "latitude": 37.5665,
  "longitude": 126.9780,
  "placeName": "서울역",
  "regionDong": "회현동",
  "regionGu": "중구",
  "images": [
    { "imageUrl": "https://cdn.example.com/product-posts/1.jpg" },
    { "imageUrl": "https://cdn.example.com/product-posts/2.jpg" }
  ],
  "documents": [
    { "documentType": "RECEIPT", "imageUrl": "https://cdn.example.com/docs/receipt.jpg" }
  ]
}
```

### Response
`201 Created`

| 필드 | 타입 | 설명 |
|------|------|------|
| productPostUuid | string | 판매글 UUID |
| memberUuid | string | 판매자 UUID |
| categoryUuid | string | 카테고리 UUID |
| productPostName | string | 상품명 |
| conditionGrade | string | 상태 등급 |
| price | number | 가격 |
| description | string | 상세 설명 |
| tradeStatus | string | 생성 시 `SELLING` |
| productPostStatus | string | 생성 시 `PUBLIC` |
| latitude | number | 위도 |
| longitude | number | 경도 |
| regionDong | string\|null | 거래 희망 동(읍면동). FE 표시 1순위 |
| regionGu | string\|null | 거래 희망 구(시군구). FE 표시 2순위 |
| placeName | string | 장소명. FE 표시 3순위 |
| createdAt | string | ISO-8601 |
| images | array | 저장된 이미지 요약 (`sortOrder` 포함, 서버가 부여한 값) |
| documents | array | 저장된 서류 요약 |

```json
{
  "productPostUuid": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
  "memberUuid": "22222222-2222-2222-2222-222222222222",
  "categoryUuid": "11111111-1111-1111-1111-111111111111",
  "productPostName": "빈티지 백",
  "conditionGrade": "A",
  "price": 550000,
  "description": "상태 좋은 빈티지 백입니다.",
  "tradeStatus": "SELLING",
  "productPostStatus": "PUBLIC",
  "latitude": 37.5665,
  "longitude": 126.9780,
  "regionDong": "회현동",
  "regionGu": "중구",
  "placeName": "서울역",
  "createdAt": "2026-08-10T01:00:00Z",
  "images": [
    {
      "productPostImageUuid": "img-uuid",
      "imageUrl": "https://cdn.example.com/product-posts/1.jpg",
      "sortOrder": 1
    }
  ],
  "documents": [
    {
      "productPostDocumentUuid": "doc-uuid",
      "documentType": "RECEIPT",
      "imageUrl": "https://cdn.example.com/docs/receipt.jpg"
    }
  ]
}
```

> 요청에는 `sortOrder`가 없고, 응답의 `sortOrder`는 배열 위치 기준으로 서버가 채운 값이다.
> `regionDong` / `regionGu`는 목록 카드와 동일 의미·필명이다. 수정·visibility·trade-status·bump·상세 응답도 동일 Summary 필드를 사용한다.
### Errors

| status | code | 의미 |
|--------|------|------|
| 400 | VALIDATION_FAILED | 요청 필드 검증 실패 (최소가 미만 포함, 설정값 기준) |
| 400 | INVALID_ARGUMENT | Domain 규칙 위반 등 |
| 400 | INVALID_MEDIA_KEY | 미디어 URL/key 형식 오류 |
| 400 | MEDIA_OBJECT_NOT_FOUND | pending 객체가 S3에 없음 |
| 401 | UNAUTHORIZED | `X-Member-Uuid` 없음 |
| 403 | FORBIDDEN | 타인 pending/정식 미디어 key |
| 500 | MEDIA_STORAGE_FAILED | S3 Copy·Delete 실패 |

---

## PUT /api/v1/product-posts/{productPostUuid}

### Summary
판매글을 수정한다. (FO, 판매자 본인)

### Method · Path
`PUT /api/v1/product-posts/{productPostUuid}`

### Auth
필요. Gateway가 넣는 헤더:

| Header | 필수 | 설명 |
|--------|------|------|
| `X-Member-Uuid` | Y | 판매자 회원 UUID (본인 글만 수정) |

### Request

| 위치 | 필드 | 타입 | 필수 | 제약 |
|------|------|------|------|------|
| Path | productPostUuid | string | Y | 판매글 UUID |
| Body | categoryUuid | string | Y | 리프 카테고리 UUID |
| Body | productPostName | string | Y | 2~100자 |
| Body | conditionGrade | string | Y | S / A / B / C |
| Body | price | number | Y | `product-post.policy.min-price` 이상 |
| Body | description | string | Y | 최대 2000자 |
| Body | latitude | number | Y | 위도 |
| Body | longitude | number | Y | 경도 |
| Body | placeName | string | Y | 최대 100자 |
| Body | regionDong | string | N | 거래 희망 동(읍면동). 최대 50자. blank/미전달 시 null 저장 |
| Body | regionGu | string | N | 거래 희망 구(시군구). 최대 50자. blank/미전달 시 null 저장 |
| Body | images | array | Y | 1~10개. **전체 교체**. 배열 순서 = 노출 순서, 빠진 기존 이미지는 soft delete |
| Body | images[].imageUrl | string | Y | 최대 500자. Presign pending URL 또는 유지할 정식 `posts/` URL |
| Body | documents | array | N | **전체 교체**. 빈 배열이면 기존 서류 전부 soft delete |
| Body | documents[].documentType | string | Y(항목 시) | `WARRANTY` \| `RECEIPT` \| `APPRAISAL` |
| Body | documents[].imageUrl | string | Y(항목 시) | 최대 500자 |

수정 가능 조건:
- `tradeStatus` = `SELLING` (예약중·거래완료는 수정 불가, 거래상태 API 별도)
- `productPostStatus` = `PUBLIC` 또는 `HIDDEN` (삭제·DELETED 불가)
- 판매자 본인 (`X-Member-Uuid` = 글의 `memberUuid`)

```json
{
  "categoryUuid": "11111111-1111-1111-1111-111111111111",
  "productPostName": "빈티지 백 (수정)",
  "conditionGrade": "A",
  "price": 520000,
  "description": "가격 조정했습니다.",
  "latitude": 37.5665,
  "longitude": 126.9780,
  "placeName": "서울역",
  "regionDong": "회현동",
  "regionGu": "중구",
  "images": [
    { "imageUrl": "https://cdn.example.com/product-posts/1-new.jpg" },
    { "imageUrl": "https://cdn.example.com/product-posts/2-new.jpg" }
  ],
  "documents": []
}
```

### Response
`200 OK`

등록 API 응답과 동일 필드 (`tradeStatus`·`productPostStatus`는 변경되지 않음).

### Errors

| status | code | 의미 |
|--------|------|------|
| 400 | VALIDATION_FAILED | 요청 필드 검증 실패 |
| 400 | INVALID_ARGUMENT | Domain 규칙 위반 (예: 판매중이 아님, 최소가 미만) |
| 400 | INVALID_MEDIA_KEY | 미디어 URL/key 형식 오류 |
| 400 | MEDIA_OBJECT_NOT_FOUND | pending 객체가 S3에 없음 |
| 401 | UNAUTHORIZED | `X-Member-Uuid` 없음 |
| 403 | FORBIDDEN | 판매자 본인이 아님, 또는 타인 미디어 key |
| 404 | PRODUCT_POST_NOT_FOUND | UUID 없음 또는 삭제됨 |
| 500 | MEDIA_STORAGE_FAILED | S3 Copy·Delete 실패 |

---

## DELETE /api/v1/product-posts/{productPostUuid}

### Summary
판매글을 Soft Delete 한다. (FO, 판매자 본인)

DB에서 물리 삭제하지 않고 `productPostStatus=DELETED`, `deletedAt` 기록. 목록·상세·신규 채팅에서 제외.

### Method · Path
`DELETE /api/v1/product-posts/{productPostUuid}`

### Auth
필요.

| Header | 필수 | 설명 |
|--------|------|------|
| `X-Member-Uuid` | Y | 판매자 회원 UUID |

### Request

| 위치 | 필드 | 타입 | 필수 | 제약 |
|------|------|------|------|------|
| Path | productPostUuid | string | Y | 판매글 UUID |

Body 없음.

### Response
`204 No Content` (본문 없음)

### Errors

| status | code | 의미 |
|--------|------|------|
| 401 | UNAUTHORIZED | `X-Member-Uuid` 없음 |
| 403 | FORBIDDEN | 판매자 본인이 아님 |
| 404 | PRODUCT_POST_NOT_FOUND | UUID 없음 또는 이미 삭제됨 |

삭제 가능 조건:
- `productPostStatus` ≠ `DELETED`, `deletedAt` = null
- `tradeStatus` ∈ `SELLING` \| `RESERVED` \| `SOLD_OUT` (거래상태와 무관하게 삭제 가능)

---

## PATCH /api/v1/product-posts/{productPostUuid}/visibility

### Summary
판매글 노출 상태를 변경한다. (FO, 숨김·재공개)

### Method · Path
`PATCH /api/v1/product-posts/{productPostUuid}/visibility`

### Auth
필요.

| Header | 필수 | 설명 |
|--------|------|------|
| `X-Member-Uuid` | Y | 판매자 회원 UUID |

### Request

| 위치 | 필드 | 타입 | 필수 | 제약 |
|------|------|------|------|------|
| Path | productPostUuid | string | Y | 판매글 UUID |
| Body | productPostStatus | string | Y | `HIDDEN` \| `PUBLIC` (`DELETED` 불가) |

```json
{ "productPostStatus": "HIDDEN" }
```

재공개:

```json
{ "productPostStatus": "PUBLIC" }
```

변경 가능 조건:
- 판매자 본인
- `productPostStatus` ≠ `DELETED`, `deletedAt` = null
- `tradeStatus` 무관 (예약·거래완료 글도 숨김·재공개 가능)
- 이미 같은 상태여도 `200 OK` (멱등)

`HIDDEN` 시 목록·상세에서 404와 동일하게 미노출. 판매자는 수정 API로 내용 변경 가능.

### Response
`200 OK`

등록 API 응답과 동일 필드 (`productPostStatus` 반영).

### Errors

| status | code | 의미 |
|--------|------|------|
| 400 | VALIDATION_FAILED | `productPostStatus` 누락 |
| 400 | INVALID_ARGUMENT | `DELETED` 등 허용되지 않은 값 |
| 401 | UNAUTHORIZED | `X-Member-Uuid` 없음 |
| 403 | FORBIDDEN | 판매자 본인이 아님 |
| 404 | PRODUCT_POST_NOT_FOUND | UUID 없음 또는 삭제됨 |

---

## PATCH /api/v1/product-posts/{productPostUuid}/trade-status

### Summary
판매글 거래 상태를 변경한다. (FO, 판매자 본인·판매관리)

내용 수정(`PUT`)과 분리. `SELLING` 상태에서만 내용 수정 가능.

### Method · Path
`PATCH /api/v1/product-posts/{productPostUuid}/trade-status`

### Auth
필요.

| Header | 필수 | 설명 |
|--------|------|------|
| `X-Member-Uuid` | Y | 판매자 회원 UUID |

### Request

| 위치 | 필드 | 타입 | 필수 | 제약 |
|------|------|------|------|------|
| Path | productPostUuid | string | Y | 판매글 UUID |
| Body | tradeStatus | string | Y | `SELLING` \| `RESERVED` \| `SOLD_OUT` |

예약중:

```json
{ "tradeStatus": "RESERVED" }
```

거래완료:

```json
{ "tradeStatus": "SOLD_OUT" }
```

예약 취소(판매중 복귀):

```json
{ "tradeStatus": "SELLING" }
```

허용 전이:

| 현재 | 변경 가능 |
|------|-----------|
| `SELLING` | `RESERVED`, `SOLD_OUT` |
| `RESERVED` | `SELLING`, `SOLD_OUT` |
| `SOLD_OUT` | (없음, 되돌리기 불가) |

변경 가능 조건:
- 판매자 본인
- `productPostStatus` ≠ `DELETED`, `deletedAt` = null
- `HIDDEN`·`PUBLIC` 모두 가능
- 이미 같은 `tradeStatus`여도 `200 OK` (멱등)

Reservation 서비스 연동(예약 확정 시 자동 `RESERVED`)은 후속 작업. 본 API는 FO 판매자 수동 변경용.

### Response
`200 OK`

등록 API 응답과 동일 필드 (`tradeStatus` 반영).

### Errors

| status | code | 의미 |
|--------|------|------|
| 400 | VALIDATION_FAILED | `tradeStatus` 누락 |
| 400 | INVALID_ARGUMENT | 허용되지 않은 전이 (예: `SOLD_OUT` → `SELLING`) |
| 401 | UNAUTHORIZED | `X-Member-Uuid` 없음 |
| 403 | FORBIDDEN | 판매자 본인이 아님 |
| 404 | PRODUCT_POST_NOT_FOUND | UUID 없음 또는 삭제됨 |

---

## POST /api/v1/product-posts/{productPostUuid}/bump

### Summary
판매글을 끌올한다. (FO, 판매자 본인)

목록에서 상단으로 재노출. `bumpedAt`이 갱신되어 정렬 기준 시각이 변경된다.

### Method · Path
`POST /api/v1/product-posts/{productPostUuid}/bump`

### Auth
필요.

| Header | 필수 | 설명 |
|--------|------|------|
| `X-Member-Uuid` | Y | 판매자 회원 UUID |

### Request

| 위치 | 필드 | 타입 | 필수 | 제약 |
|------|------|------|------|------|
| Path | productPostUuid | string | Y | 판매글 UUID |

Body 없음.

끌올 가능 조건:
- 판매자 본인
- `tradeStatus` = `SELLING`
- `productPostStatus` = `PUBLIC`
- 동일 상품 쿨다운 경과 (기본 12시간, 환경변수 `PRODUCT_POST_BUMP_COOLDOWN_HOURS`)
- 일일 끌올 한도 미초과 (기본 2회, 환경변수 `PRODUCT_POST_BUMP_DAILY_LIMIT`)

### Response
`200 OK`

등록 API 응답과 동일 필드 (`bumpedAt` 반영).

```json
{
  "productPostUuid": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
  "bumpedAt": "2026-08-18T08:00:00Z",
  "tradeStatus": "SELLING",
  "productPostStatus": "PUBLIC"
}
```

### Errors

| status | code | 의미 |
|--------|------|------|
| 400 | INVALID_ARGUMENT | 판매중·공개 아님, 쿨다운 미경과, 일일 한도 초과 |
| 401 | UNAUTHORIZED | `X-Member-Uuid` 없음 |
| 403 | FORBIDDEN | 판매자 본인이 아님 |
| 404 | PRODUCT_POST_NOT_FOUND | UUID 없음 또는 삭제됨 |

---

## GET /api/v1/product-posts/{productPostUuid}

### Summary
판매글 상세를 조회한다. (FO, 공개글만)

### Method · Path
`GET /api/v1/product-posts/{productPostUuid}`

### Auth
불필요. 미인증 사용자도 공개 판매글을 조회할 수 있다.

### Request

| 위치 | 필드 | 타입 | 필수 | 제약 |
|------|------|------|------|------|
| Path | productPostUuid | string | Y | 판매글 UUID |

### Response
`200 OK`

등록 API 응답과 동일 필드. 활성 이미지만 `sortOrder` 오름차순으로 포함하며, 삭제된 이미지는 제외한다.

```json
{
  "productPostUuid": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
  "memberUuid": "22222222-2222-2222-2222-222222222222",
  "categoryUuid": "11111111-1111-1111-1111-111111111111",
  "productPostName": "빈티지 백",
  "conditionGrade": "A",
  "price": 550000,
  "description": "상태 좋은 빈티지 백입니다.",
  "tradeStatus": "SELLING",
  "productPostStatus": "PUBLIC",
  "latitude": 37.5665,
  "longitude": 126.9780,
  "regionDong": "회현동",
  "regionGu": "중구",
  "placeName": "서울역",
  "createdAt": "2026-08-10T01:00:00Z",
  "images": [
    {
      "productPostImageUuid": "img-uuid",
      "imageUrl": "https://cdn.example.com/product-posts/1.jpg",
      "sortOrder": 1
    }
  ],
  "documents": [
    {
      "productPostDocumentUuid": "doc-uuid",
      "documentType": "RECEIPT",
      "imageUrl": "https://cdn.example.com/docs/receipt.jpg"
    }
  ]
}
```

### Errors

| status | code | 의미 |
|--------|------|------|
| 404 | PRODUCT_POST_NOT_FOUND | UUID 없음, 또는 HIDDEN·DELETED (존재 여부 구분 없음) |

---

## GET /api/v1/product-posts

### Summary
판매글 목록을 조회한다. (FO 홈·헤더 검색·필터·페이징)

헤더의 추천·연관 검색어 API는 [search-api.md](./search-api.md) 참고. `keyword` 검색 시 Redis 카운터는 비동기로 적재된다.

### Method · Path
`GET /api/v1/product-posts`

### Auth
불필요.

### Request (Query)

| 필드 | 타입 | 필수 | 제약 |
|------|------|------|------|
| categoryUuids | string[] | N | 리프 카테고리 UUID. 없으면 전체(All). Luxury「전체상품」은 FE가 하위 리프 UUID를 모두 전달 |
| memberUuid | string | N | 판매자(회원) UUID. 해당 회원의 PUBLIC 글만. 마이페이지·프로필 「판매중인 물품」용. blank면 미적용 |
| tradeStatus | string | N | `SELLING`/`RESERVED`/`SOLD_OUT`. 미전달 시 세 상태 전체. `memberUuid` 없이도 홈 피드 필터로 사용 가능 |
| keyword | string | N | 상품명 FULLTEXT(ngram) 검색. 빈 값이면 미적용. **2글자 미만**이면 결과 없음 |
| minPrice | number | N | 0 이상. maxPrice보다 클 수 없음 |
| maxPrice | number | N | 0 이상 |
| conditionGrades | string[] | N | `S`/`A`/`B`/`C`. 없으면 전체 |
| documentTypes | string[] | N | `WARRANTY`/`RECEIPT`/`APPRAISAL`. 선택값 중 **하나라도** 가진 글(OR). 삭제된 서류 제외 |
| page | number | N | 1-based, 기본 `1` |
| size | number | N | 기본 `20`, 최대 `50` |

정렬: `COALESCE(bumpedAt, createdAt) DESC` (끌올 반영 최신순)  
노출: `productPostStatus=PUBLIC` 이고 `tradeStatus` ∈ 요청값(또는 미전달 시 `SELLING`\|`RESERVED`\|`SOLD_OUT`) (HIDDEN·DELETED 제외)  
`totalElements` / `totalPages`는 **필터 적용 후** 건수·페이지 수.

```http
GET /api/v1/product-posts?categoryUuids=uuid1&documentTypes=RECEIPT&documentTypes=WARRANTY&page=1&size=20
GET /api/v1/product-posts?memberUuid=550e8400-e29b-41d4-a716-446655440000&page=1&size=4
GET /api/v1/product-posts?memberUuid=550e8400-e29b-41d4-a716-446655440000&tradeStatus=SELLING&page=1&size=20
GET /api/v1/product-posts?memberUuid=550e8400-e29b-41d4-a716-446655440000&tradeStatus=RESERVED&page=1&size=20
GET /api/v1/product-posts?memberUuid=550e8400-e29b-41d4-a716-446655440000&tradeStatus=SOLD_OUT&page=1&size=20
GET /api/v1/product-posts?tradeStatus=SELLING&page=1&size=20
```
### Response
`200 OK`

| 필드 | 타입 | 설명 |
|------|------|------|
| content | array | 카드 목록 |
| content[].productPostUuid | string | 판매글 UUID |
| content[].productPostName | string | 제목 |
| content[].price | number | 가격 |
| content[].tradeStatus | string | `SELLING`/`RESERVED`/`SOLD_OUT` (카드 뱃지) |
| content[].listedAt | string | 목록 기준 시각 ISO-8601 (FE 상대 시간) |
| content[].thumbnailUrl | string\|null | 대표 이미지 URL |
| content[].regionDong | string\|null | 거래 희망 동(읍면동). FE 표시 1순위 |
| content[].regionGu | string\|null | 거래 희망 구(시군구). FE 표시 2순위 |
| content[].placeName | string | 거래 희망 장소명. FE 표시 3순위 |
| page | number | 현재 페이지 (1-based) |
| size | number | 페이지 크기 |
| totalElements | number | 전체 건수 |
| totalPages | number | 전체 페이지 수 |

```json
{
  "content": [
    {
      "productPostUuid": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
      "productPostName": "판매글 제목입니다",
      "price": 1500000,
      "tradeStatus": "SELLING",
      "listedAt": "2026-08-12T01:00:00Z",
      "thumbnailUrl": "https://cdn.example.com/product-posts/1.jpg",
      "regionDong": "초량동",
      "regionGu": "동구",
      "placeName": "부산역"
    }
  ],
  "page": 1,
  "size": 20,
  "totalElements": 48,
  "totalPages": 3
}
```

마이페이지 카피 권장: `regionDong ?? regionGu ?? placeName` → `{값}에서 거래`

### Errors

| status | code | 의미 |
|--------|------|------|
| 400 | INVALID_ARGUMENT | page/size/가격 범위/등급/서류 종류/`tradeStatus` 값 오류 |
