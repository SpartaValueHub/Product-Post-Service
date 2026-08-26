# 예약 이벤트 수신 (Kafka)

Reservations가 예약을 등록하면 Product-Post가 `tradeStatus`를 `RESERVED`로 바꾼다.
홈 피드·검색·마이페이지·상세 등 판매글 조회 API는 이미 `tradeStatus`를 내려주므로, 카드 뱃지(예약중)는 별도 API 추가 없이 반영된다.

## Summary
예약 등록 이벤트 수신 후 해당 판매글을 예약중으로 변경

## Method · Path
Kafka consume `reservation.events` (HTTP 아님)

## Auth
없음 (내부 이벤트). 판매자 `X-Member-Uuid` 검사 없음.

## Request
토픽 키: `productPostUuid`

| 필드 | 타입 | 필수 | 사용 |
|------|------|------|------|
| eventType | string | Y | `CREATED`만 거래상태 변경. 그 외는 skip |
| productPostUuid | string | Y | 판매글 UUID |
| 나머지 | - | N | 무시 |

```json
{
  "eventType": "CREATED",
  "productPostUuid": "11111111-1111-1111-1111-111111111111",
  "reservationUuid": "...",
  "chatRoomUuid": "...",
  "meetAt": "2026-08-26T12:00:00+09:00",
  "placeName": "...",
  "sellerUuid": "...",
  "buyerUuid": "...",
  "updatedAt": "2026-08-26T01:00:00Z"
}
```

## Response
없음 (비동기). 성공 시 `tradeStatus = RESERVED`.

이후 조회:

| API | 뱃지 필드 |
|-----|-----------|
| `GET /api/v1/product-posts` 목록 카드 | `content[].tradeStatus` |
| `GET /api/v1/product-posts/{uuid}` 상세 | `tradeStatus` |

`RESERVED`는 기본 목록 필터(미전달 시 `SELLING`/`RESERVED`/`SOLD_OUT`)에 포함된다.

## Errors
HTTP 응답 없음. 컨슈머는 아래를 로그 후 skip 한다 (무한 재시도 없음).

| 상황 | 동작 |
|------|------|
| JSON 파싱 실패 | skip |
| eventType 미매핑 (`UPDATED`, `CANCELED` 등) | skip |
| 판매글 없음·삭제됨 | skip |
| `SOLD_OUT` 등 전이 불가 | skip |
| 이미 `RESERVED` | 멱등 성공 (저장 생략) |

DB·브로커 일시 오류는 예외를 그대로 던져 재시도한다.

## 설정
- group-id: `listing-service`
- bootstrap: compose `SPRING_KAFKA_BOOTSTRAP_SERVERS` (prod `kafka:19092`)
- eventType 매핑: `product-post.kafka.event-type-trade-status`
