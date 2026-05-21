# Asset Management Backend

개인 자산·부채·투자를 통합 관리하는 Spring Boot REST API 서버.  
보유 종목의 실시간 주가와 뉴스를 수집하고, Claude AI가 매일 투자 리포트를 생성합니다.

## 주요 기능

- **자산 / 부채 관리** — CRUD, 월납입 자동 처리, 정렬 순서 변경
- **투자 포트폴리오** — Yahoo Finance 실시간 주가, 환율 적용 평가액·수익률 계산
- **AI 리포트** — 보유 종목 관련 뉴스 수집 → Claude 요약 → OpenAI 벡터 임베딩 → Pinecone 저장 → Claude HTML 리포트 생성 (SSE 실시간 스트리밍)
- **대시보드** — 자산 유형별 차트, 투자 카테고리별 차트, 기간별 순자산 추이 스냅샷

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 3.4.3 |
| DB | PostgreSQL 15 + Flyway |
| ORM | Spring Data JPA + QueryDSL 5 |
| Auth | JWT (HttpOnly Cookie) |
| AI | Claude API (claude-sonnet-4-6), OpenAI Embeddings |
| Vector DB | Pinecone |
| News | Naver News API (국내), Google RSS (해외) |
| Deploy | GitHub Actions → Docker Hub → EC2 |

## 로컬 실행

**사전 요구사항**: JDK 17, Docker

```bash
# 1. PostgreSQL 실행
docker-compose up -d

# 2. application-local.yml 작성 (아래 환경변수 섹션 참고)

# 3. 서버 시작
./gradlew bootRun --args='--spring.profiles.active=local'
```

서버: `http://localhost:8080`

## 환경변수 (application-local.yml)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/asset_db
    username: asset_user
    password: asset1234

claude:
  api:
    key: <ANTHROPIC_API_KEY>

news:
  naver:
    client-id: <NAVER_CLIENT_ID>
    client-secret: <NAVER_CLIENT_SECRET>
  google-rss:
    base-url: https://news.google.com/rss/search

openai:
  api-key: <OPENAI_API_KEY>
  embedding-url: https://api.openai.com/v1/embeddings

pinecone:
  api-key: <PINECONE_API_KEY>
  host: <PINECONE_HOST>
  index-name: <PINECONE_INDEX_NAME>
```

> `application-local.yml`은 실제 키를 포함하므로 `.gitignore`에 추가해야 합니다.

## 프로덕션 환경변수

| 변수명 | 설명 |
|--------|------|
| `DB_URL` | PostgreSQL JDBC URL |
| `DB_USERNAME` / `DB_PASSWORD` | DB 인증 |
| `ANTHROPIC_API_KEY` | Claude API 키 |
| `JWT_SECRET` | JWT 서명 키 |
| `NAVER_CLIENT_ID` / `NAVER_CLIENT_SECRET` | 네이버 뉴스 API |
| `GOOGLE_RSS_BASE_URL` | Google RSS URL |
| `OPENAI_API_KEY` / `OPENAI_EMBEDDING_URL` | OpenAI 임베딩 |
| `PINECONE_API_KEY` / `PINECONE_HOST` / `PINECONE_INDEX_NAME` | Pinecone |

## API 개요!

| 도메인 | Base URL | 설명 |
|--------|----------|------|
| 인증 | `/api/auth` | 회원가입, 로그인, 토큰 갱신, 로그아웃 |
| 자산 | `/api/assets` | CRUD, 대시보드, 투자 동기화, 순서 변경 |
| 부채 | `/api/debts` | CRUD, 요약 |
| 투자 | `/api/investments` | CRUD, 대시보드 요약/차트 |
| 리포트 | `/api/reports` | AI 리포트 생성 (SSE 스트리밍), 조회 |
| 스냅샷 | `/api/snapshots` | 기간별 순자산 추이 |
| 설정 | `/api/config` | 자산/투자 카테고리, 소유자 관리 |
| 뉴스 | `/api/news` | 수동 뉴스 수집·임베딩 |

상세 스펙: [`docs/API_기능정의서.md`](docs/API_기능정의서.md)

## 배포

`main` 브랜치 push 시 GitHub Actions가 자동으로:
1. Gradle 빌드 (JAR)
2. Docker 이미지 빌드 → Docker Hub push
3. EC2 SSH 접속 → 컨테이너 재시작

프로덕션: `https://yoojoo-asset-management.xyz`
