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

- **asset** — core financial assets with type `FIXED | REGULAR | VARIABLE | RETIREMENT | INVESTMENT`, monthly payment tracking, and `sortOrder` for user-defined ordering
- **debt** — liabilities with similar structure to assets
- **investment** — individual stock/ETF holdings linked to an `Asset` via `@ManyToOne`; real-time prices fetched from Yahoo Finance (`StockPriceService`); overseas holdings use Caffeine-cached (24h TTL) USD/KRW exchange rate (`ExchangeRateService`)
- **daily_report** — AI-generated reports via Claude API (`ClaudeApiService`, model `claude-sonnet-4-6`); SSE streaming pipeline: news collect → embed → report (`GET /api/reports/generate/stream`)
- **news** — collects articles from Naver News API for top-10 holdings by purchase amount; summarises with Claude API and upserts embeddings to Pinecone vector DB via OpenAI text-embedding model
- **snapshot** — `AssetDailySnapshot` (daily asset/debt totals) + `InvestmentCategorySnapshot` (daily per-category investment amounts), both written by nightly scheduler
- **config** — user-managed lists: asset categories, asset owners, investment categories (seeded at signup)
- **auth** — JWT cookie auth (`access_token` 15 min, `refresh_token` 7 days, HttpOnly)

### Common Package

`src/main/java/com/asset/asset_backend/common/`:
- **response/** — `ApiResult<T>` wraps all API responses (`success`/`error` static factories); `ApiPageResponse` for paginated results
- **exception/** — `BaseException` + `ErrorCode` enum + `GlobalExceptionHandler`
- **enums/** — `AssetType`, `MarketType` (DOMESTIC / OVERSEAS)
- **config/** — `AppConfig` (RestTemplate bean), `CorsConfig`, `QueryDslConfig` (JPAQueryFactory bean)
- 스케줄러는 `common/scheduler/`에 두지 않고 각 도메인 폴더 내 `scheduler/` 하위에 생성한다 (예: `domains/asset/scheduler/AssetScheduler.java`)

### Key Patterns

- **Entities** use static factory methods (`Asset.createAsset(...)`) and `@NoArgsConstructor(access = PROTECTED)`. No setters — updates go through domain methods like `updateAssetInfo(...)`, `updateSortOrder(...)`.
- **Services** are `@Transactional(readOnly = true)` at class level; individual write methods override with `@Transactional`.
- **QueryDSL** custom queries: each repository has a `XxxRepositoryCustom` interface and `XxxRepositoryCustomImpl`. The `Impl` class must extend the `Custom` interface and be named exactly `XxxRepositoryCustomImpl` for Spring Data to auto-detect it.
- **Reorder logic** (`AssetService.reorderAsset`): shifts `sortOrder` of affected rows up or down, then sets the moved item's `sortOrder` to `targetPosition`.
- **DTO 폴더 구조** — `dto/request/`: API 요청 객체, `dto/response/`: API 응답 객체 (suffix `Response` 필수). 내부 계산용 객체는 suffix 없이 기능 단위 폴더로 분리 (예: `dto/portfolio/Holding.java`).
- **Service 레이어 DTO 변환** — Entity가 없는 집계성 Response는 `static from(값, 값, ...)` 정적 팩토리 메서드를 DTO 안에 정의하고, Service에서 `XxxResponse.from(...)` 형태로 호출. `builder()`는 DTO 내부 팩토리 메서드 안에서만 사용하며, Service에서 직접 호출하지 않는다. 중첩 객체도 동일하게 `of(...)` 팩토리 메서드 적용.

### 뉴스 수집 → 임베딩 → 리포트 생성 파이프라인

`GET /api/reports/generate/stream` (SSE)은 별도 스레드에서 아래 순서로 실행된다:

#### 1단계 COLLECT — 뉴스 수집 (`NewsCollectorService`)
- 매수금액 상위 10개 종목 조회 (`Investment.getTopByPurchaseAmount`)
- 국내 종목(`DOMESTIC`): 네이버 뉴스 API (`https://openapi.naver.com/v1/search/news`) — query=종목명, display=5, sort=sim
- 해외 종목(`OVERSEAS`): Google RSS (`https://news.google.com/rss/search`) — XML 파싱
- 기사 본문은 Jsoup으로 크롤링 (`#dic_area`, `#article-view-content-div` 등 셀렉터 순차 시도); 실패 시 description fallback
- 중복 URL 및 14일 초과 기사 스킵 → `NewsArticle` 엔티티로 DB 저장 (`embedded=false`)

#### 2단계 EMBED — 요약 + 벡터 임베딩 (`NewsCollectorService.embedAll`)
- `embedded=false`인 기사 전체 조회
- **Claude API** (`ClaudeApiService.generateSummary`, max 512 토큰): 투자자 관점 3줄 요약 생성 (기사 본문 최대 1000자 전달)
- **OpenAI API** (`EmbeddingService`): 요약문을 `text-embedding-ada-002`로 벡터화 → `List<Float>`
- **Pinecone upsert** (`PineconeService`): namespace=`news`, vector ID=`news_{articleId}`, metadata에 `title`, `url`, `source`, `ticker`, `summary`, `publishedAt`(epoch) 저장
- 성공 시 `article.markEmbedded()` 호출 (`embedded=true` 업데이트)

#### 3단계 REPORT — 리포트 생성 (`DailyReportService`)
- **Pinecone query**: 종목별로 `"{종목명} 주가 실적 투자 리스크 전망 시장 영향"` 쿼리 → OpenAI 임베딩 변환 후 유사도 검색, topK=2, filter: `ticker` + `publishedAt >= 7일 전`
- 조회된 뉴스 요약 (`summary`, `title`)을 프롬프트에 `[관련 뉴스]` 섹션으로 삽입
- **Claude API** (`generateReport`, max 4000 토큰): HTML 5섹션 구조 (`market-summary`, `stock-analysis`, `portfolio`, `recommendation`, `action`) 생성
- **Claude API** (`generateReport`, max 4000 토큰): 텍스트 요약 브리핑 (`📈 날짜 | 시장 | 주목 | 액션 | 추천`) 생성
- `DailyReport` 엔티티로 DB 저장; 당일 리포트가 이미 있으면 생성 없이 기존 반환

각 단계는 `ProgressEvent` 레코드(`type`, `status`, `ticker`, `stockName`, `message`, `current`, `total`, `data`)로 클라이언트에 진행률을 SSE 전송한다.

### Profiles & Config

- `application.yml` — base config (PostgreSQL dialect, Flyway enabled, port 8080, JWT secret)
- `application-local.yml` — local datasource + `show-sql: true`; `claude.api.key`, `news.naver.*`, `openai.api-key`, `pinecone.*` 설정 필요
- `application-prod.yml` — reads from env vars: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `ANTHROPIC_API_KEY`

> **주의**: `application-local.yml`은 실제 API 키를 포함하므로 `.gitignore`에 반드시 추가해야 한다.

### Deployment

GitHub Actions (`.github/workflows/deploy.yml`) on push to `main`: builds JAR → Docker image → pushes to Docker Hub → SSH deploys to EC2.