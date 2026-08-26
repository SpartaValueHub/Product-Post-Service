# ProductPost Service 설정 컨벤션

Discovery / Gateway / Auth-Service 와 동일한 YAML 프로필 구조를 사용합니다.

## YAML 파일 구조

| 파일 | 역할 |
|------|------|
| `application.yml` | 모든 환경 공통 설정 (앱명, JPA, Eureka, springdoc) |
| `application-local.yml` | 로컬 개발 (팀 공통, Git 포함) |
| `application-dev.yml` | 통합/개발 서버 |
| `application-prod.yml` | 운영 서버 |

## 프로필 활성화

```yaml
spring.profiles.active: ${SPRING_PROFILES_ACTIVE:local}
```

- 개인 PC: 기본 `local`
- 통합 검증 노트북: `SPRING_PROFILES_ACTIVE=dev`
- 배포: `SPRING_PROFILES_ACTIVE=prod`

## 포트 및 Eureka

- `server.port: 0` — OS가 할당하는 사용 가능한 랜덤 포트
- Eureka instance-id: `서비스명:실제포트` (예: `product-post-service:51234`) — 기동 로그와 Eureka 대시보드에서 확인
- `eureka.instance.prefer-ip-address: true`

## DB (MSA 스키마 분리)

Auth-Service 와 **동일한 MySQL 인스턴스**를 사용하며, **스키마만 분리**합니다.

| 서비스 | 스키마 |
|--------|--------|
| auth-service | `auth_db` |
| member-service | `member_db` |
| member-regions-service | `member_regions_db` |
| product-post-service | `product_post_db` |

로컬 JDBC URL 예시:

```
jdbc:mysql://localhost:3307/product_post_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul&characterEncoding=UTF-8
```

## Secret 관리 규칙

**YAML 파일에 비밀번호, DB 계정을 직접 작성하지 않습니다.**

| 항목 | local | dev / prod |
|------|-------|------------|
| DB URL / 계정 / 비밀번호 | `.env` | 배포 환경변수 |
| Eureka URL (local) | `application-local.yml` (`localhost:8761`) | `${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE}` |

### 로컬 실행 준비

```bash
cp .env.example .env
# .env 파일에 실제 값 입력
```

개인별 override가 필요하면 Git에 포함되지 않는 `application-local-secret.yml` 을 추가할 수 있습니다.

## 로컬 실행

```bash
# 1. Discovery 실행
cd ../discovery && ./gradlew bootRun

# 2. .env 설정 후 ProductPost Service 실행
cd ../Listing-Service && ./gradlew bootRun
```

- `bootRun` 은 프로젝트 루트의 `.env` 를 자동 로드합니다.
- IDE에서 실행할 때는 **Run Configuration** `ProductPostServiceApplication` 을 사용하거나, `ProductPostServiceApplication.java` 에서 main 실행 시 Working directory 가 프로젝트 루트인지 확인하세요.
- `application-local.yml` 이 `optional:file:.env[.properties]` 를 import 하므로 IDE main 실행 시에도 `.env` 가 로드됩니다.

Eureka Dashboard (`http://localhost:8761`) 에서 `PRODUCT-POST-SERVICE` 등록을 확인합니다.

## Redis (헤더 검색 추천·카운터)

Auth / Gateway와 **동일 Redis**를 사용합니다. Product-Post에 Redis 컨테이너를 따로 두지 않습니다.

| 환경 | 준비 |
|------|------|
| local | Auth-Service 루트 `docker compose up -d` (redis:6379) 후 `.env`에 아래 변수 |
| Apps EC2 | 기존 Auth용 Redis + compose `.env`에 동일 변수 |

| 변수 | 설명 | 예시 |
|------|------|------|
| `REDIS_HOST` | Redis 호스트 | `127.0.0.1` |
| `REDIS_PORT` | Redis 포트 | `6379` |

## 수동 SQL 스크립트 (`scripts/`)

Hibernate `ddl-auto=update` 로 안 잡히거나 prod `validate` 전에 필요한 인덱스·컬럼:

| 파일 | 용도 |
|------|------|
| `add-product-post-list-indexes.sql` | 목록 복합 인덱스 |
| `add-product-post-region-labels.sql` | 동·구 컬럼 |
| `add-product-post-name-fulltext.sql` | 제목 FULLTEXT(ngram) — keyword 검색 |

keyword 검색 배포 전 **반드시** `add-product-post-name-fulltext.sql` 을 `product_post_db`에 적용한다.

## 필수 환경변수 (local)

| 변수 | 설명 |
|------|------|
| `SPRING_DATASOURCE_URL` | MySQL JDBC URL (`product_post_db` 스키마) |
| `SPRING_DATASOURCE_USERNAME` | DB 사용자 |
| `SPRING_DATASOURCE_PASSWORD` | DB 비밀번호 |
| `REDIS_HOST` / `REDIS_PORT` | 검색어 카운터·인기 검색 (Auth와 동일) |

## 필수 환경변수 (dev / prod)

| 변수 | 설명 |
|------|------|
| `SPRING_DATASOURCE_URL` | MySQL JDBC URL (`product_post_db` 스키마) |
| `SPRING_DATASOURCE_USERNAME` | DB 사용자 |
| `SPRING_DATASOURCE_PASSWORD` | DB 비밀번호 |
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | Eureka Server URL |
| `REDIS_HOST` / `REDIS_PORT` | Auth와 동일 Redis (Apps EC2 localhost) |

## 패키지 구조 (Hexagonal)

```
com.sparta.product_post_service/
├── domain/model/              # 도메인 모델
├── application/
│   ├── service/               # Use Case 구현
│   ├── exception/
│   └── port/
│       ├── in/ (+ dto/)       # 인바운드 포트
│       └── out/               # 아웃바운드 포트
├── adaptor/
│   ├── in/web/                # REST API (Controller, VO, Mapper)
│   └── out/mysql/             # JPA (Entity, Repository, Adapter)
└── config/                    # 인프라 설정
```

엔티티·도메인·비즈니스 로직은 기능 개발 시 추가합니다.
