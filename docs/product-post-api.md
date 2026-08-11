# ProductPost API (FE 연동 스펙)

Listing-Service 판매글(ProductPost) API 명세서입니다.  
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
| images | array | Y | 1~10개. **배열 순서 = 노출 순서**(0번 인덱스가 대표/썸네일). 서버가 `sort_order` 1..n 부여 |
| images[].imageUrl | string | Y | 최대 500자 (업로드 URL) |
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
| placeName | string | 장소명 |
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
### Errors

| status | code | 의미 |
|--------|------|------|
| 400 | VALIDATION_FAILED | 요청 필드 검증 실패 (최소가 미만 포함, 설정값 기준) |
| 400 | INVALID_ARGUMENT | Domain 규칙 위반 등 |
| 401 | UNAUTHORIZED | `X-Member-Uuid` 없음 |
