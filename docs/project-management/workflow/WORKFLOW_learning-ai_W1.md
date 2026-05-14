# WORKFLOW: @learning-ai-owner — Week 1

> **Task 문서**: [TASK_learning-ai.md](../task/TASK_learning-ai.md)  
> **기간**: 2026-05-12 ~ 2026-05-16  
> **기능개발 Workflow**: [README §7](../README.md)

---

## Step 1: FastAPI 프로젝트 초기 설정

### 1.1 TASK 시작
- [ ] Step Goal / Done When / Scope / Input 확인
- [ ] PRD_W1 해당 요구사항 확인 (프로젝트 골격)
- [ ] Duration 산정 확인 (0.5일)

### 1.2 요구사항 분석
- [ ] FastAPI + uvicorn 프로젝트 구조 분석
- [ ] 디렉토리 구조 설계 (app/, app/api/, app/core/, app/services/)
- [ ] Python 3.12+ 의존성 목록 도출 (pyproject.toml)
- [ ] Instructions 초안 → TASK 문서 반영

### 1.3 Security 1차 검토
- [ ] 인증 필요 여부: No (골격만 생성, Health endpoint)
- [ ] 권한 종류: 없음
- [ ] 공개 API 여부: Yes (/health만 공개)
- [ ] 결과 → TASK Constraints 반영

### 1.4 ERD 설계
- [ ] 골격 단계 — DB 연동 해당 없음
- [ ] 프로젝트 디렉토리 구조도 작성
- [ ] Duration(final) 갱신

### 1.5 Security 2차 검토
- [ ] 민감 정보 암호화: 비해당 (골격 단계)
- [ ] .env.example 작성 (시크릿 플레이스홀더만)
- [ ] pydantic-settings 환경변수 관리 확인
- [ ] 결과 → TASK Constraints 반영

### 1.6 DTO / Entity 설계 (API First)
- [ ] HealthResponse Pydantic 모델 정의 (service, version, status)
- [ ] 공통 설정 모델 (Settings) 작성 (pydantic-settings)
- [ ] Output Format → TASK 반영

### 1.7 Repository 구현
- [ ] 골격 단계 — Repository 해당 없음
- [ ] pyproject.toml 기반 프로젝트 초기화 (Poetry 또는 uv)

### 1.8 Service + Test
- [ ] FastAPI app 인스턴스 생성
- [ ] /health 엔드포인트 구현 (200 OK + 서비스명/버전 응답)
- [ ] pytest 설정 및 health 테스트 작성
- [ ] 테스트 통과 확인

### 1.9 Controller + Test
- [ ] Dockerfile 작성 (multi-stage build)
- [ ] docker-compose.yml에 서비스 추가
- [ ] uvicorn 실행 확인 (포트 8090)

### 1.10 View + Test (해당 시)
- [ ] Flutter 화면 연동: 해당 없음
- [ ] Docker 컨테이너 실행 + /health 응답 확인
- [ ] RULE Reference → TASK 반영

**Step 1 Status**: [ ] Not Started / [ ] In Progress / [ ] Done

---

## Step 2: Claude API 연동

### 1.1 TASK 시작
- [ ] Step Goal / Done When / Scope / Input 확인
- [ ] PRD_W1 해당 요구사항 확인 (FR-AI-xxx AI 생성)
- [ ] Duration 산정 확인 (1.5일)

### 1.2 요구사항 분석
- [ ] Anthropic Python SDK 문서 분석
- [ ] 에러 핸들링 시나리오 정의 (429 Rate Limit, 500 Internal)
- [ ] Exponential backoff 재시도 정책 (1s, 2s, 4s / 최대 3회)
- [ ] Instructions 초안 → TASK 문서 반영

### 1.3 Security 1차 검토
- [ ] 인증 필요 여부: Yes (JWT 인증 필요)
- [ ] 권한 종류: 로그인 사용자
- [ ] 공개 API 여부: No
- [ ] API Key 환경변수 관리 (ANTHROPIC_API_KEY) — 하드코딩 금지
- [ ] 결과 → TASK Constraints 반영

### 1.4 ERD 설계
- [ ] Claude API 연동 — DB 테이블 해당 없음
- [ ] Request/Response 스키마 설계
- [ ] Duration(final) 갱신

### 1.5 Security 2차 검토
- [ ] 민감 정보 암호화: API Key 환경변수 전용 (코드 노출 금지)
- [ ] 사용자 입력 검증 (프롬프트 인젝션 방지)
- [ ] 응답 토큰 제한 (최대 4096)
- [ ] 결과 → TASK Constraints 반영

### 1.6 DTO / Entity 설계 (API First)
- [ ] GenerateRequest Pydantic 모델 정의 (prompt, max_tokens, temperature)
- [ ] GenerateResponse Pydantic 모델 정의 (content, model, usage)
- [ ] ErrorResponse Pydantic 모델 정의 (detail, code)
- [ ] Output Format → TASK 반영

### 1.7 Repository 구현
- [ ] DB 연동 없음 — 해당 없음
- [ ] `anthropic` Python SDK 의존성 추가 (pyproject.toml)

### 1.8 Service + Test
- [ ] ClaudeService 클래스 생성 (비동기 클라이언트)
- [ ] 429 Rate Limit 시 exponential backoff 재시도 구현
- [ ] 500 에러 시 fallback 응답 처리
- [ ] 타임아웃 30초 설정
- [ ] pytest mock 테스트 작성 (정상/429/500 시나리오)
- [ ] 테스트 통과 확인

### 1.9 Controller + Test
- [ ] POST /api/v1/ai/generate 엔드포인트 구현
- [ ] 입력값 검증 (max_tokens 제한 등)
- [ ] 에러 핸들러 미들웨어 구현
- [ ] pytest 통합 테스트 (mock 기반)
- [ ] 테스트 통과 확인

### 1.10 View + Test (해당 시)
- [ ] Flutter 화면 연동: 해당 없음 (프론트 별도)
- [ ] OpenAPI(Swagger) 문서 자동 생성 확인
- [ ] RULE Reference → TASK 반영

**Step 2 Status**: [ ] Not Started / [ ] In Progress / [ ] Done

---

## Step 3: Embedding API 연동

### 1.1 TASK 시작
- [ ] Step Goal / Done When / Scope / Input 확인
- [ ] PRD_W1 해당 요구사항 확인 (FR-AI-xxx 임베딩)
- [ ] Duration 산정 확인 (2일)

### 1.2 요구사항 분석
- [ ] OpenAI Embedding API 문서 분석 (text-embedding-3-small)
- [ ] 벡터 차원 확인 (1536차원)
- [ ] 배치 임베딩 요건 (최대 20건 동시)
- [ ] Instructions 초안 → TASK 문서 반영

### 1.3 Security 1차 검토
- [ ] 인증 필요 여부: Yes (JWT 인증 필요)
- [ ] 권한 종류: 로그인 사용자
- [ ] 공개 API 여부: No
- [ ] API Key 환경변수 관리 (OPENAI_API_KEY) — 하드코딩 금지
- [ ] 결과 → TASK Constraints 반영

### 1.4 ERD 설계 (pgvector 스키마)
- [ ] embeddings 테이블 설계 (id, source_id, source_type, vector(1536), created_at)
- [ ] pgvector 확장 활성화 (`CREATE EXTENSION vector`)
- [ ] 인덱스 설계 (ivfflat 또는 hnsw)
- [ ] Alembic 마이그레이션 작성
- [ ] Duration(final) 갱신

### 1.5 Security 2차 검토
- [ ] 민감 정보 암호화: API Key 환경변수 전용
- [ ] 입력 텍스트 길이 검증 (최대 8192 토큰)
- [ ] 벡터 저장 시 normalize 적용
- [ ] 결과 → TASK Constraints 반영

### 1.6 DTO / Entity 설계 (API First)
- [ ] EmbedRequest Pydantic 모델 정의 (texts: List[str])
- [ ] EmbedResponse Pydantic 모델 정의 (embeddings: List[List[float]], model)
- [ ] Embedding SQLAlchemy 모델 작성 (pgvector Column)
- [ ] Output Format → TASK 반영

### 1.7 Repository 구현
- [ ] EmbeddingRepository 클래스 작성 (async SQLAlchemy)
- [ ] pgvector 확장 + embeddings 테이블 Alembic 마이그레이션
- [ ] `openai` Python SDK 의존성 추가

### 1.8 Service + Test
- [ ] EmbeddingService 클래스 생성 (비동기)
- [ ] 텍스트 → 1536차원 벡터 변환 로직
- [ ] 배치 임베딩 지원 (최대 20개)
- [ ] 입력 텍스트 길이 검증
- [ ] pytest mock 테스트 작성 (단일/배치/에러 시나리오)
- [ ] 테스트 통과 확인

### 1.9 Controller + Test
- [ ] POST /api/v1/ai/embed 엔드포인트 구현
- [ ] 배치 크기 검증 (최대 20건)
- [ ] 에러 핸들링 (OpenAI API 에러)
- [ ] pytest 통합 테스트 (mock 기반)
- [ ] 테스트 통과 확인

### 1.10 View + Test (해당 시)
- [ ] Flutter 화면 연동: 해당 없음 (프론트 별도)
- [ ] OpenAPI(Swagger) 문서 자동 생성 확인
- [ ] RULE Reference → TASK 반영

**Step 3 Status**: [ ] Not Started / [ ] In Progress / [ ] Done
