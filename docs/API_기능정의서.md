# API 기능정의서

> **기준 버전**: Spring Boot 3.4.3 / Java 17
> **Base URL**: `/api`
> **인증 방식**: JWT (HttpOnly 쿠키 `access_token`)
> **공통 응답 형식**:
> ```json
> { "success": true, "data": {}, "message": "..." }
> { "success": false, "error": { "code": "...", "message": "...", "details": "..." } }
> ```

---

## 목차

1. [인증 (Auth)](#1-인증-auth)
2. [자산 (Asset)](#2-자산-asset)
3. [부채 (Debt)](#3-부채-debt)
4. [투자 (Investment)](#4-투자-investment) — CRUD + 대시보드
5. [일일 리포트 (Daily Report)](#5-일일-리포트-daily-report)
6. [스냅샷 (Snapshot)](#6-스냅샷-snapshot)
7. [사용자 설정 (Config)](#7-사용자-설정-config)
8. [스케줄러 (Scheduler)](#8-스케줄러-scheduler)

---

## 1. 인증 (Auth)

### 1-1. 회원가입

| 항목 | 내용 |
|------|------|
| **URL** | `POST /api/auth/signup` |
| **인증 필요** | X |

**요청 바디**
```json
{
  "loginId": "string (필수)",
  "password": "string (필수)"
}
```

**응답 바디**
```json
{
  "data": {
    "id": 1,
    "loginId": "user123",
    "createdAt": "2026-03-27T00:00:00"
  }
}
```

**비즈니스 로직**
- `loginId` 중복 여부 검사 → 중복 시 예외
- 비밀번호 BCrypt 인코딩 후 저장
- 회원가입 완료 시 기본 설정값 자동 insert (`UserConfig`):
  - ASSET_CATEGORY: 전세보증금, 청약저축, 청년도약적금, IRP, DC, 연금저축, 기타
  - INVESTMENT_CATEGORY: ETF, 주식, 금, 현금, 기타

---

### 1-2. 로그인

| 항목 | 내용 |
|------|------|
| **URL** | `POST /api/auth/login` |
| **인증 필요** | X |

**요청 바디**
```json
{
  "loginId": "string (필수)",
  "password": "string (필수)"
}
```

**응답**
- 바디: `data: null`
- 쿠키 (HttpOnly, Secure, SameSite=Lax):
  - `access_token` — TTL 15분
  - `refresh_token` — TTL 7일

**비즈니스 로직**
- loginId로 사용자 조회 → BCrypt 비밀번호 검증
- JWT accessToken / refreshToken 생성
- 기존 RefreshToken 삭제 후 신규 저장

---

### 1-3. 토큰 갱신

| 항목 | 내용 |
|------|------|
| **URL** | `POST /api/auth/refresh` |
| **인증 필요** | X (refresh_token 쿠키 필요) |

**응답**
- 쿠키: `access_token` (신규 발급)

**비즈니스 로직**
- `refresh_token` 쿠키 추출 → DB에서 RefreshToken 조회
- 만료 및 JWT 유효성 검증 → 새 accessToken 발급

---

### 1-4. 로그아웃

| 항목 | 내용 |
|------|------|
| **URL** | `POST /api/auth/logout` |
| **인증 필요** | O |

**응답**: `data: null`

**비즈니스 로직**
- DB에서 해당 유저의 RefreshToken 전부 삭제
- `access_token` / `refresh_token` 쿠키 제거 (MaxAge=0)

---

### 1-5. 내 정보 조회

| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/auth/me` |
| **인증 필요** | O |

**응답 바디**
```json
{
  "data": {
    "id": 1,
    "loginId": "user123",
    "createdAt": "2026-03-27T00:00:00"
  }
}
```

---

## 2. 자산 (Asset)

### 2-1. 자산 생성

| 항목 | 내용 |
|------|------|
| **URL** | `POST /api/assets` |
| **인증 필요** | O |

**요청 바디**
```json
{
  "category": "string (필수)",
  "owner": "string",
  "amount": 10000000,
  "type": "FIXED | REGULAR | VARIABLE | RETIREMENT | INVESTMENT (필수)",
  "monthlyPayment": 300000,
  "paymentDay": 25,
  "note": "string",
  "linkedToInvestment": false
}
```

**응답 바디**
```json
{
  "data": {
    "id": 1,
    "category": "청년도약적금",
    "owner": "본인",
    "amount": 10000000,
    "type": "REGULAR",
    "monthlyPayment": 300000,
    "paymentDay": 25,
    "note": "",
    "linkedToInvestment": false,
    "sortOrder": 5,
    "createdAt": "2026-03-27T00:00:00",
    "updatedAt": "2026-03-27T00:00:00"
  }
}
```

**비즈니스 로직**
- 현재 최대 `sortOrder` 조회 → +1로 신규 자산에 할당 (목록 맨 아래 추가)

---

### 2-2. 자산 목록 조회

| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/assets` |
| **인증 필요** | O |

**쿼리 파라미터**
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| category | String | X | 카테고리 필터 |
| owner | String | X | 소유자 필터 |
| type | AssetType | X | 자산 유형 필터 |
| page | int | X | 페이지 번호 (기본값: 0) |
| size | int | X | 페이지 크기 (기본값: 10) |

**응답 바디**
```json
{
  "data": {
    "content": [ /* AssetResponse[] */ ],
    "totalElements": 20,
    "totalPages": 2,
    "pageNumber": 0,
    "pageSize": 10,
    "hasNextPage": true,
    "hasPreviousPage": false
  }
}
```

**비즈니스 로직**
- QueryDSL로 동적 필터링 (null 조건 자동 무시)
- `sortOrder` 오름차순 정렬

---

### 2-3. 자산 단건 조회

| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/assets/{id}` |
| **인증 필요** | O |

**경로 파라미터**: `id` (자산 ID)

**비즈니스 로직**: 조회 후 요청자 소유권 검증

---

### 2-4. 자산 수정

| 항목 | 내용 |
|------|------|
| **URL** | `PUT /api/assets/{id}` |
| **인증 필요** | O |

**요청 바디**: 2-1 생성 요청과 동일

**비즈니스 로직**: 소유권 검증 후 `updateAssetInfo()` 도메인 메서드로 업데이트

---

### 2-5. 자산 삭제

| 항목 | 내용 |
|------|------|
| **URL** | `DELETE /api/assets/{id}` |
| **인증 필요** | O |

**비즈니스 로직**: 소유권 검증 후 DB 삭제

---

### 2-6. 투자 연동 자산 목록 조회

| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/assets/linked` |
| **인증 필요** | O |

**응답 바디**: `data: AssetResponse[]`

**비즈니스 로직**: `linkedToInvestment = true`인 자산 전체 조회 (페이징 없음)

---

### 2-7. 자산 순서 변경

| 항목 | 내용 |
|------|------|
| **URL** | `PATCH /api/assets/{id}/reorder` |
| **인증 필요** | O |

**요청 바디**
```json
{
  "targetPosition": 3
}
```
> `targetPosition`: 1-based 인덱스 (이동할 목표 위치)

**비즈니스 로직**
- 현재 위치 vs 목표 위치 비교
  - 아래로 이동: 사이 구간 `sortOrder` 감소
  - 위로 이동: 사이 구간 `sortOrder` 증가
- 대상 자산의 `sortOrder`를 `targetPosition`으로 설정

---

### 2-8. 대시보드 요약 조회

| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/assets/dashboard/summary` |
| **인증 필요** | O |

**응답 바디**
```json
{
  "data": {
    "totalAmount": 50000000,
    "totalMonthlyPayment": 700000,
    "retirementAmount": 20000000,
    "investmentAmount": 15000000
  }
}
```

**비즈니스 로직**: 사용자의 전체 자산 집계 (합산)

---

### 2-9. 대시보드 차트 데이터 조회

| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/assets/dashboard/chart` |
| **인증 필요** | O |

**응답 바디**
```json
{
  "data": {
    "items": [
      { "type": "FIXED",      "amount": 10000000, "percentage": 20.0 },
      { "type": "REGULAR",    "amount": 15000000, "percentage": 30.0 },
      { "type": "RETIREMENT", "amount": 20000000, "percentage": 40.0 },
      { "type": "INVESTMENT", "amount": 5000000,  "percentage": 10.0 }
    ]
  }
}
```

**비즈니스 로직**: 자산 유형별 금액 집계 → 전체 대비 비율 계산 (소수점 1자리)

---

### 2-10. 자산별 투자 금액 동기화

| 항목 | 내용 |
|------|------|
| **URL** | `POST /api/assets/{assetId}/sync-investments` |
| **인증 필요** | O |

**경로 파라미터**: `assetId` (자산 ID)

**비즈니스 로직**
- 소유권 검증 후 해당 Asset에 연결된 Investment 전체의 평가금액 합산
- Asset의 `amount` 필드를 합산값으로 업데이트

---

### 2-11. 전체 투자 연동 자산 동기화

| 항목 | 내용 |
|------|------|
| **URL** | `POST /api/assets/sync-all` |
| **인증 필요** | O |

**비즈니스 로직**
- 현재 유저의 `linkedToInvestment = true`인 자산 전체 조회
- 각 자산에 대해 `syncAssetAmount()` 순차 호출

---

## 3. 부채 (Debt)

### 3-1. 부채 생성

| 항목 | 내용 |
|------|------|
| **URL** | `POST /api/debts` |
| **인증 필요** | O |

**요청 바디**
```json
{
  "category": "string (필수)",
  "owner": "string",
  "amount": 50000000,
  "type": "FIXED | REGULAR | VARIABLE (필수)",
  "monthlyPayment": 500000,
  "paymentDay": 10,
  "purpose": "string",
  "note": "string"
}
```

**응답 바디**: DebtResponse (id, category, owner, amount, type, monthlyPayment, paymentDay, purpose, note, createdAt, updatedAt)

---

### 3-2. 부채 목록 조회

| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/debts` |
| **인증 필요** | O |

**쿼리 파라미터**: `category`, `owner`, `type`, `page` (기본값 0), `size` (기본값 10)

**응답 바디**: `ApiPageResponse<DebtResponse>`

---

### 3-3. 부채 단건 조회

| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/debts/{id}` |
| **인증 필요** | O |

---

### 3-4. 부채 수정

| 항목 | 내용 |
|------|------|
| **URL** | `PUT /api/debts/{id}` |
| **인증 필요** | O |

**요청 바디**: 3-1 생성 요청과 동일

---

### 3-5. 부채 삭제

| 항목 | 내용 |
|------|------|
| **URL** | `DELETE /api/debts/{id}` |
| **인증 필요** | O |

---

### 3-6. 부채 요약 조회

| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/debts/summary` |
| **인증 필요** | O |

**응답 바디**
```json
{
  "data": {
    "totalAmount": 80000000,
    "totalMonthlyPayment": 1200000
  }
}
```

---

## 4. 투자 (Investment)

### 4-1. 투자 항목 생성

| 항목 | 내용 |
|------|------|
| **URL** | `POST /api/investments` |
| **인증 필요** | O |

**요청 바디**
```json
{
  "assetId": 1,
  "category": "ETF",
  "stockName": "TIGER 미국S&P500",
  "ticker": "360750.KS",
  "owner": "본인",
  "purchasePrice": 15000,
  "quantity": 100,
  "purchaseAmount": 1500000,
  "marketType": "DOMESTIC | OVERSEAS"
}
```

**응답 바디**
```json
{
  "data": {
    "id": 1,
    "assetId": 1,
    "account": "KB증권 (Asset.category)",
    "category": "ETF",
    "stockName": "TIGER 미국S&P500",
    "ticker": "360750.KS",
    "owner": "본인",
    "purchasePrice": 15000,
    "quantity": 100,
    "purchaseAmount": 1500000,
    "marketType": "DOMESTIC",
    "currentPrice": 16500,
    "exchangeRate": null,
    "evaluationAmount": 1650000,
    "profitRate": 10.0,
    "createdAt": "2026-03-27T00:00:00",
    "updatedAt": "2026-03-27T00:00:00"
  }
}
```

**비즈니스 로직**
- 연동 Asset 소유권 검증
- Yahoo Finance API로 현재가 조회 (`StockPriceService`)
- `OVERSEAS`인 경우 USD/KRW 환율 조회 (Caffeine 캐시 24h TTL, `ExchangeRateService`)
- 평가금액 = 현재가 × 수량 (해외: 원화 환산), 수익률 = (평가금액 - 매수금액) / 매수금액 × 100

---

### 4-2. 투자 항목 목록 조회

| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/investments` |
| **인증 필요** | O |

**쿼리 파라미터**
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| owner | String | X | 소유자 필터 |
| category | String | X | 카테고리 필터 |
| assetId | Long | X | 연동 자산 ID 필터 |
| page | int | X | 기본값 0 |
| size | int | X | 기본값 10 |

**비즈니스 로직**
- QueryDSL 동적 필터링
- 페이지 내 해외 종목 존재 시 환율 일괄 조회 (1회 호출)

---

### 4-3. 투자 항목 단건 조회

| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/investments/{id}` |
| **인증 필요** | O |

---

### 4-4. Asset별 투자 항목 전체 조회

| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/investments/asset/{assetId}` |
| **인증 필요** | O |

**응답 바디**: `data: InvestmentResponse[]` (페이징 없음)

---

### 4-5. 투자 항목 수정

| 항목 | 내용 |
|------|------|
| **URL** | `PUT /api/investments/{id}` |
| **인증 필요** | O |

**요청 바디**: 4-1 생성 요청과 동일

---

### 4-6. 투자 항목 삭제

| 항목 | 내용 |
|------|------|
| **URL** | `DELETE /api/investments/{id}` |
| **인증 필요** | O |

---

### 4-7. 투자 대시보드 요약 조회

| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/investments/dashboard/summary` |
| **인증 필요** | O |

**응답 바디**
```json
{
  "data": {
    "totalAmount": 20000000,
    "categories": [
      { "category": "ETF",  "amount": 12000000, "percentage": 60.0 },
      { "category": "주식", "amount": 6000000,  "percentage": 30.0 },
      { "category": "금",   "amount": 2000000,  "percentage": 10.0 }
    ]
  }
}
```

**비즈니스 로직**
- `AssetType.INVESTMENT`인 자산에 연결된 Investment 전체 조회
- 각 Investment의 현재가 조회 (`StockPriceService`) → 평가금액 계산 (OVERSEAS: 환율 적용)
- `investment.category`별 평가금액 합산
- 전체 합계 대비 각 카테고리 비율 계산 (소수점 1자리)

---

### 4-8. 투자 대시보드 차트 데이터 조회

| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/investments/dashboard/chart` |
| **인증 필요** | O |

**쿼리 파라미터**
| 파라미터 | 타입 | 기본값 | 설명 |
|----------|------|--------|------|
| period | String | 30d | `7d` \| `30d` \| `90d` \| `1y` |

**응답 바디**
```json
{
  "data": {
    "period": "30d",
    "data": [
      {
        "snapshotDate": "2026-03-01",
        "categories": [
          { "category": "ETF",  "amount": 11000000 },
          { "category": "주식", "amount": 5500000 }
        ]
      }
    ]
  }
}
```

**비즈니스 로직**
- period → startDate 변환 (`7d`: -7일, `30d`: -30일, `90d`: -90일, `1y`: -1년)
- `AssetDailySnapshot` 날짜 범위 조회 (차트의 날짜 기준)
- 각 날짜의 카테고리별 금액은 `InvestmentCategorySnapshot` 테이블에서 조회
- N+1 방지: 날짜 범위 내 카테고리 스냅샷 전체 일괄 조회 후 날짜별 그룹핑

---

## 5. 일일 리포트 (Daily Report)

### 5-1. 리포트 생성 (수동)

| 항목 | 내용 |
|------|------|
| **URL** | `POST /api/reports/generate` |
| **인증 필요** | O |

**응답 바디**
```json
{
  "data": {
    "id": 1,
    "reportDate": "2026-03-27",
    "fullContent": "<section id=\"market-summary\">...</section>...",
    "summaryContent": "오늘 포트폴리오 요약...",
    "createdAt": "2026-03-27T00:00:00"
  }
}
```

**비즈니스 로직**
1. 오늘 날짜 리포트가 이미 존재하면 그대로 반환 (중복 생성 방지)
2. 없으면 신규 생성:
   - 사용자의 Investment 목록 조회 (없으면 예외)
   - 종목 정보(종목명, 계좌, 카테고리, 소유자, 매수단가, 수량) 텍스트 포맷팅
   - Claude API 호출:
     - **전체 리포트** (`fullContent`): HTML 4개 섹션 구조 (market-summary, stock-analysis, portfolio-outlook, risk-management)
     - **요약 리포트** (`summaryContent`): 최대 1,500 토큰 제한
   - DB 저장

---

### 5-2. 리포트 목록 조회

| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/reports` |
| **인증 필요** | O |

**응답 바디**: `data: DailyReportResponse[]` (최신순)

---

### 5-3. 날짜별 리포트 조회

| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/reports/{date}` |
| **인증 필요** | O |

**경로 파라미터**: `date` — ISO 날짜 형식 (`2026-03-27`)

**비즈니스 로직**: `reportDate + userId`로 조회 → 없으면 404 예외

---

## 6. 스냅샷 (Snapshot)

> 스냅샷은 두 테이블로 관리됩니다:
> - `asset_daily_snapshot` — 날짜별 자산/부채/순자산 총합
> - `investment_category_snapshot` — 날짜별 투자 카테고리별 금액

### 6-1. 자산 일일 스냅샷 조회

| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/snapshots` |
| **인증 필요** | O |

**쿼리 파라미터**
| 파라미터 | 타입 | 기본값 | 설명 |
|----------|------|--------|------|
| period | String | 30d | `7d` \| `30d` \| `90d` \| `1y` |

**응답 바디**
```json
{
  "data": [
    {
      "snapshotDate": "2026-03-27",
      "totalAssetAmount": 50000000,
      "retirementAmount": 20000000,
      "investmentAmount": 15000000,
      "totalDebtAmount": 30000000,
      "netWorthAmount": 20000000
    }
  ]
}
```

**비즈니스 로직**
- period → startDate 변환 (`7d`: -7일, `30d`: -30일, `90d`: -90일, `1y`: -1년)
- `startDate ~ 오늘` 범위의 스냅샷 날짜 오름차순 반환

**스냅샷 생성 (자동)**
- 매일 자정 (`AssetScheduler`) 스케줄러 실행:
  1. `linkedToInvestment=true` 자산에 연결된 Investment 현재가 동기화 (`syncAssetAmount`)
  2. 전체/유형별 자산 합계 + 부채 합계 집계 후 `AssetDailySnapshot` 저장

---

## 7. 사용자 설정 (Config)

> 자산 카테고리 / 자산 소유자 / 투자 카테고리 3종류를 동일한 구조로 관리합니다.
> **기본값은 회원가입 시 DB에 insert되므로** 별도 하드코딩 없이 사용자 데이터로 관리됩니다.

### 설정 종류별 URL

| 설정 종류 | URL prefix | 회원가입 시 기본 삽입값 |
|----------|-----------|--------|
| 자산 카테고리 | `/api/config/asset-categories` | 전세보증금, 청약저축, 청년도약적금, IRP, DC, 연금저축, 기타 |
| 자산 소유자 | `/api/config/asset-owners` | (없음) |
| 투자 카테고리 | `/api/config/investment-categories` | ETF, 주식, 금, 현금, 기타 |

---

### 7-1. 목록 조회

| 항목 | 내용 |
|------|------|
| **URL** | `GET /api/config/{type}` |
| **인증 필요** | O |

**응답 바디**
```json
{
  "data": [
    { "id": 1, "value": "IRP" },
    { "id": 2, "value": "ETF" },
    { "id": 3, "value": "사용자 추가값" }
  ]
}
```

**비즈니스 로직**: 해당 userId의 설정값만 반환 (생성시간 오름차순)

---

### 7-2. 항목 추가

| 항목 | 내용 |
|------|------|
| **URL** | `POST /api/config/{type}` |
| **인증 필요** | O |

**요청 바디**
```json
{ "value": "추가할 이름" }
```

**비즈니스 로직**
- 동일 사용자 + 동일 타입 + 동일 값 중복 검사 → 중복 시 예외

---

### 7-3. 항목 삭제

| 항목 | 내용 |
|------|------|
| **URL** | `DELETE /api/config/{type}/{id}` |
| **인증 필요** | O |

**비즈니스 로직**: 소유권 검증 후 삭제

---

## 8. 스케줄러 (Scheduler)

> 모든 스케줄러는 **매일 자정 (`0 0 0 * * *`)** 자동 실행됩니다.
> 수동 실행은 각 도메인 API를 통해 트리거할 수 있습니다.

---

### 8-1. 자산 월납입 처리 (AssetPaymentScheduler)

| 항목 | 내용 |
|------|------|
| **실행 주기** | 매일 자정 |
| **수동 실행 API** | `POST /api/assets/scheduler/payment` |

**처리 로직**
1. 오늘 날짜의 `dayOfMonth` 값과 `paymentDay`가 일치하는 자산 조회
2. `monthlyPayment`가 있는 자산에 한해 `amount += monthlyPayment` 처리

**예시**: `paymentDay=25`인 자산은 매월 25일 자정에 `monthlyPayment` 금액만큼 `amount` 증가

---

### 8-2. 부채 월상환 처리 (DebtPaymentScheduler)

| 항목 | 내용 |
|------|------|
| **실행 주기** | 매일 자정 |
| **수동 실행 API** | `POST /api/debts/scheduler/payment` |

**처리 로직**
1. 오늘 날짜의 `dayOfMonth` 값과 `paymentDay`가 일치하는 부채 조회
2. `monthlyPayment`가 있는 부채에 한해 `amount -= monthlyPayment` 처리

**예시**: `paymentDay=10`인 부채는 매월 10일 자정에 `monthlyPayment` 금액만큼 `amount` 감소

---

### 8-3. 일일 자산 스냅샷 저장 (AssetScheduler)

| 항목 | 내용 |
|------|------|
| **실행 주기** | 매일 자정 |
| **수동 실행 API** | `POST /api/admin/scheduler/daily-snapshot` |

**처리 로직** (사용자별 반복)
1. `linkedToInvestment=true`인 자산에 연결된 Investment 현재가를 Yahoo Finance API로 조회 후 `amount` 동기화
2. 전체 자산 합계 / RETIREMENT 합계 / INVESTMENT 합계 / 전체 부채 합계 집계
3. `netWorthAmount = 총자산 - 총부채` 계산
4. `AssetDailySnapshot` upsert (당일 기존 row 있으면 업데이트)
5. 투자 카테고리별 평가금액 집계 후 `InvestmentCategorySnapshot` upsert (당일 기존 row 삭제 후 재삽입)

---

## 공통 에러 코드

| 코드 | HTTP 상태 | 설명 |
|------|-----------|------|
| `NOT_FOUND` | 404 | 리소스를 찾을 수 없음 |
| `FORBIDDEN` | 403 | 접근 권한 없음 (소유권 불일치) |
| `UNAUTHORIZED` | 401 | 인증 필요 |
| `INVALID_INPUT` | 400 | 잘못된 요청 파라미터 |
| `DUPLICATE` | 409 | 중복 데이터 |
| `INTERNAL_ERROR` | 500 | 서버 내부 오류 |

---

## AssetType 열거값

| 값 | 설명 |
|----|------|
| `FIXED` | 고정형 (예금, 전세보증금 등) |
| `REGULAR` | 정기 납입형 (적금 등) |
| `VARIABLE` | 변동형 |
| `RETIREMENT` | 노후대비 (IRP, DC, 연금저축 등) |
| `INVESTMENT` | 투자 (증권 계좌 등) |

## MarketType 열거값

| 값 | 설명 |
|----|------|
| `DOMESTIC` | 국내 (KRW 기준, 환율 적용 안 함) |
| `OVERSEAS` | 해외 (USD 기준, 실시간 환율 적용) |
