# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Start local DB
docker-compose up -d

# Build (skip tests)
./gradlew clean build -x test

# Build with tests
./gradlew build

# Run tests
./gradlew test

# Run a single test class
./gradlew test --tests "com.asset.asset_backend.SomeTest"

# Run the application (local profile)
./gradlew bootRun --args='--spring.profiles.active=local'
```

## Architecture

Spring Boot 3.4.3 / Java 17 personal finance management API. PostgreSQL with Flyway migrations, QueryDSL for dynamic queries.

### Domain Structure

All domains live under `src/main/java/com/asset/asset_backend/domains/`:

- **asset** — core financial assets (deposits, savings accounts, etc.) with type `FIXED | REGULAR | VARIABLE`, monthly payment tracking, and `sortOrder` for user-defined ordering
- **debt** — liabilities with similar structure to assets
- **investment** — individual holdings (stocks/ETFs) linked to an `Asset` via `@ManyToOne`; real-time prices fetched from Yahoo Finance API in `StockPriceService`
- **daily_report** — AI-generated daily investment reports using Claude API (`ClaudeApiService`); reports are generated once per day and cached in the DB

### Common Package

`src/main/java/com/asset/asset_backend/common/`:
- **response/** — `ApiResult<T>` wraps all API responses (`success`/`error` static factories); `ApiPageResponse` for paginated results
- **exception/** — `BaseException` + `ErrorCode` enum + `GlobalExceptionHandler`
- **enums/** — `AssetType` (FIXED, REGULAR, VARIABLE)
- **scheduler/** — `AssetPaymentScheduler` and `DebtPaymentScheduler` run daily at midnight, adding `monthlyPayment` to assets/debts whose `paymentDay` matches today
- **config/** — `AppConfig` (RestTemplate bean), `CorsConfig`, `QueryDslConfig` (JPAQueryFactory bean)

### Key Patterns

- **Entities** use static factory methods (`Asset.createAsset(...)`) and `@NoArgsConstructor(access = PROTECTED)`. No setters — updates go through domain methods like `updateAssetInfo(...)`, `updateSortOrder(...)`.
- **Services** are `@Transactional(readOnly = true)` at class level; individual write methods override with `@Transactional`.
- **QueryDSL** custom queries: each repository has a `XxxRepositoryCustom` interface and `XxxRepositoryCustomImpl`. The `Impl` class must extend the `Custom` interface and be named exactly `XxxRepositoryCustomImpl` for Spring Data to auto-detect it.
- **Reorder logic** (`AssetService.reorderAsset`): shifts `sortOrder` of affected rows up or down, then sets the moved item's `sortOrder` to `targetPosition`.
- **DTO 폴더 구조** — `dto/request/`: API 요청 객체, `dto/response/`: API 응답 객체 (suffix `Response` 필수). 내부 계산용 객체는 suffix 없이 기능 단위 폴더로 분리 (예: `dto/portfolio/Holding.java`).

### Profiles & Config

- `application.yml` — base config (PostgreSQL dialect, Flyway enabled, port 8080)
- `application-local.yml` — local datasource + `show-sql: true`; requires `claude.api.key` for daily reports
- `application-prod.yml` — reads DB credentials from env vars `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and `ANTHROPIC_API_KEY`

### Deployment

GitHub Actions (`.github/workflows/deploy.yml`) on push to `main`: builds JAR → Docker image → pushes to Docker Hub → SSH deploys to EC2.
