# TASK: @learning-ai-owner

> **담당 서비스**: learning-ai-svc
> **GitHub Repository**: [synapse-learning-svc](https://github.com/team-project-final/synapse-learning-svc) (`learning-ai` 모듈)
> **주차**: W1 (2026-05-12 ~ 2026-05-16)
> **관련 문서**: [SCOPE](../scope/SCOPE_learning-ai.md) | [PRD_W1](../prd/PRD_W1.md) | [WORKFLOW](../workflow/WORKFLOW_learning-ai_W1.md) | [HISTORY](../history/HISTORY_learning-ai.md)

---

## Step 1: 프로젝트 초기 설정

| 필드 | 내용 |
|------|------|
| **Step Name** | 프로젝트 초기 설정 |
| **Step Goal** | learning-ai-owner가 FastAPI + uvicorn 기반 learning-ai 프로젝트를 생성하여 Health endpoint와 기본 구조가 동작한다. |
| **Done When** | uvicorn → /health 200 OK + pytest 실행 + Dockerfile 빌드 |
| **Scope** | **In**: FastAPI 프로젝트 구조, uvicorn 설정, Health endpoint, Dockerfile / **Out**: AI 모델 연동, 비즈니스 로직, DB 연동 |
| **Input** | FastAPI 공식 문서, 팀 Python 프로젝트 표준 구조, PRD_W1 서비스 요구사항 |
| **Instructions** | 1. `pyproject.toml` 기반 프로젝트 초기화 (Poetry 또는 uv)<br>2. FastAPI + uvicorn 의존성 설정<br>3. 프로젝트 디렉토리 구조 생성 (`app/`, `app/api/`, `app/core/`, `app/services/`)<br>4. `/health` 엔드포인트 구현 (200 OK + 서비스명/버전 응답)<br>5. pytest 설정 및 health 테스트 작성<br>6. `Dockerfile` 작성 (multi-stage build)<br>7. `docker-compose.yml`에 서비스 추가<br>8. `.env.example` 및 설정 관리 (pydantic-settings) |
| **Output Format** | 프로젝트 디렉토리 구조 + Health 응답 JSON + Docker 빌드 로그 |
| **Constraints** | - Python 3.12+<br>- FastAPI 0.115+<br>- 포트: 8090<br>- async/await 기반 핸들러<br>- 타입 힌트 100% 적용 |
| **Duration** | 0.5일 |
| **RULE Reference** | [18-기술-스택](../../wiki/18-기술-스택.md) · [10-환경-설정](../../wiki/10-환경-설정.md) · [14-배포-가이드](../../wiki/14-배포-가이드.md) |
| **Assignee** | @learning-ai-owner |
| **Reviewer** | @tech-lead |

---

## Step 2: Claude API 연동

| 필드 | 내용 |
|------|------|
| **Step Name** | Claude API 연동 |
| **Step Goal** | learning-ai 서비스가 Anthropic Claude API를 호출하여 텍스트를 생성할 수 있다. |
| **Done When** | POST /api/v1/ai/generate → Claude 응답 + 에러 핸들링(429/500) + pytest mock 테스트 |
| **Scope** | **In**: Anthropic SDK 연동, 텍스트 생성 API, 에러 핸들링, 재시도 로직 / **Out**: 프롬프트 최적화, 스트리밍 응답, 사용량 추적 |
| **Input** | Anthropic Python SDK 문서, API Key 설정, PRD_W1 AI 기능 명세 |
| **Instructions** | 1. `anthropic` Python SDK 의존성 추가<br>2. `ClaudeService` 클래스 생성 (비동기 클라이언트)<br>3. POST `/api/v1/ai/generate` 엔드포인트 구현<br>4. Request/Response Pydantic 모델 정의<br>5. 429 (Rate Limit) 에러 시 exponential backoff 재시도 구현<br>6. 500 에러 시 fallback 응답 처리<br>7. pytest mock 테스트 작성 (정상/429/500 시나리오)<br>8. API Key를 환경변수로 관리 (ANTHROPIC_API_KEY) |
| **Output Format** | API 엔드포인트 + Request/Response 스키마 + 테스트 결과 |
| **Constraints** | - Claude 모델: claude-sonnet-4-20250514<br>- 최대 토큰: 4096<br>- 타임아웃: 30초<br>- 재시도 최대 3회 (1s, 2s, 4s 간격)<br>- API Key는 절대 코드에 하드코딩 금지 |
| **Duration** | 1.5일 |
| **RULE Reference** | [03-아키텍처](../../wiki/03-아키텍처.md) · [18-기술-스택](../../wiki/18-기술-스택.md) |
| **Assignee** | @learning-ai-owner |
| **Reviewer** | @tech-lead |

---

## Step 3: Embedding API 연동

| 필드 | 내용 |
|------|------|
| **Step Name** | Embedding API 연동 |
| **Step Goal** | learning-ai 서비스가 텍스트를 OpenAI Embedding API로 벡터(1536차원)로 변환할 수 있다. |
| **Done When** | POST /api/v1/ai/embed → 벡터 반환 + pgvector 저장 준비 + pytest 테스트 |
| **Scope** | **In**: OpenAI Embedding API 연동, 벡터 변환, pgvector 스키마 준비 / **Out**: 유사도 검색, RAG 파이프라인, 벡터 인덱싱 최적화 |
| **Input** | OpenAI Embedding API 문서, pgvector 설정 가이드, PRD_W1 임베딩 요구사항 |
| **Instructions** | 1. `openai` Python SDK 의존성 추가<br>2. `EmbeddingService` 클래스 생성 (비동기)<br>3. POST `/api/v1/ai/embed` 엔드포인트 구현<br>4. 텍스트 → 1536차원 벡터 변환 로직<br>5. pgvector 확장 활성화 및 `embeddings` 테이블 마이그레이션 (Alembic)<br>6. 배치 임베딩 지원 (최대 20개 텍스트 동시 처리)<br>7. pytest mock 테스트 작성 (단일/배치/에러 시나리오)<br>8. 입력 텍스트 길이 검증 (최대 8192 토큰) |
| **Output Format** | API 엔드포인트 + 벡터 응답 예시 + DB 스키마 + 테스트 결과 |
| **Constraints** | - 모델: text-embedding-3-small (1536차원)<br>- 입력 최대: 8192 토큰<br>- 배치 최대: 20건<br>- pgvector 확장 필수<br>- 벡터 저장 시 normalize 적용<br>- API Key 환경변수 관리 (OPENAI_API_KEY) |
| **Duration** | 2일 |
| **RULE Reference** | [03-아키텍처](../../wiki/03-아키텍처.md) · [18-기술-스택](../../wiki/18-기술-스택.md) · [14-배포-가이드](../../wiki/14-배포-가이드.md) |
| **Assignee** | @learning-ai-owner |
| **Reviewer** | @tech-lead |

---

# W2 (2026-05-19 ~ 2026-05-23)

## Step 4: 시맨틱 검색 (pgvector)

| 필드 | 내용 |
|------|------|
| **Step Name** | 시맨틱 검색 (pgvector) |
| **Step Goal** | learning-ai 서비스가 노트 텍스트를 벡터로 변환하여 pgvector에 저장하고, 코사인 유사도 기반 시맨틱 검색을 수행할 수 있다. |
| **Done When** | 노트 벡터 저장 + 코사인 유사도 검색 API + Top-K 결과 반환 + 테스트 통과 |
| **Scope** | **In**: pgvector 벡터 저장, 코사인 유사도 검색 API, 인덱싱 / **Out**: 하이브리드 검색 병합, 검색 UI |
| **Input** | Step 3 완료된 Embedding API, pgvector 설정, PRD_W2 검색 요구사항 |
| **Instructions** | 1. pgvector 테이블에 노트별 벡터 저장 로직 구현<br>2. 코사인 유사도 기반 검색 쿼리 구현 (`<=>` 연산자)<br>3. 시맨틱 검색 API 구현 (POST `/api/v1/ai/search/semantic`)<br>4. Top-K 결과 반환 (기본 K=10)<br>5. IVFFlat 또는 HNSW 인덱스 설정<br>6. 검색 결과에 유사도 점수 포함<br>7. 통합 테스트: 벡터 저장 → 검색 → 관련성 검증 |
| **Output Format** | 시맨틱 검색 API 응답 예시 + 인덱스 설정 + 테스트 결과 |
| **Constraints** | - 코사인 유사도 사용<br>- Top-K 기본값: 10, 최대: 100<br>- 검색 응답 시간 500ms 이내<br>- HNSW 인덱스 권장 (정확도/속도 균형)<br>- 벡터 차원: 1536 (text-embedding-3-small) |
| **Duration** | 1.5일 |
| **RULE Reference** | [03-아키텍처](../../wiki/03-아키텍처.md) · [18-기술-스택](../../wiki/18-기술-스택.md) |
| **Assignee** | @learning-ai-owner |
| **Reviewer** | @tech-lead |
| **Status** | TODO |

---

## Step 5: LLM 기반 플래시카드 생성

| 필드 | 내용 |
|------|------|
| **Step Name** | LLM 기반 플래시카드 생성 |
| **Step Goal** | learning-ai 서비스가 노트 내용을 LLM에 전달하여 플래시카드(앞면/뒷면) 목록을 생성할 수 있다. |
| **Done When** | 노트 → LLM → 플래시카드 목록 생성 API + 프롬프트 테스트 + 품질 검증 |
| **Scope** | **In**: LLM 프롬프트 설계, 플래시카드 생성 API, 응답 파싱 / **Out**: 자동 저장, Kafka 이벤트 연동, 사용자 편집 |
| **Input** | Step 2 완료된 Claude API, 노트 데이터, PRD_W2 AI 카드 생성 요구사항 |
| **Instructions** | 1. 플래시카드 생성 프롬프트 설계 (노트 내용 → 앞면/뒷면 쌍 목록)<br>2. POST `/api/v1/ai/cards/generate` 엔드포인트 구현<br>3. LLM 응답을 구조화된 JSON으로 파싱<br>4. 카드 품질 검증 로직 (최소 길이, 중복 제거)<br>5. 노트 길이에 따른 카드 수 조절 (500자당 약 3-5장)<br>6. 프롬프트 버전 관리 (프롬프트 템플릿 파일 분리)<br>7. pytest 테스트: 다양한 노트 입력에 대한 카드 생성 품질 검증 |
| **Output Format** | 플래시카드 생성 API 응답 + 프롬프트 템플릿 + 테스트 결과 |
| **Constraints** | - 카드 앞면: 최대 200자<br>- 카드 뒷면: 최대 500자<br>- 노트당 생성 카드: 최소 3장 ~ 최대 20장<br>- LLM 응답 JSON 파싱 실패 시 재시도 1회<br>- 프롬프트에 한국어/영어 언어 감지 포함 |
| **Duration** | 1.5일 |
| **RULE Reference** | [03-아키텍처](../../wiki/03-아키텍처.md) · [18-기술-스택](../../wiki/18-기술-스택.md) |
| **Assignee** | @learning-ai-owner |
| **Reviewer** | @tech-lead |
| **Status** | TODO |

---

# W3 (2026-05-26 ~ 2026-05-29, 5/25 부처님오신날 제외 — Kafka 소비 + RAG 골격)

## Step 6: note.created Kafka 이벤트 소비 및 자동 카드 생성

| 필드 | 내용 |
|------|------|
| **Step Name** | note.created Kafka 이벤트 소비 및 자동 카드 생성 |
| **Step Goal** | note.created Kafka 이벤트를 소비하여 자동으로 AI 카드를 생성하고 learning-card API로 저장한다. |
| **Done When** | Kafka Consumer 동작 + 이벤트 수신 → AI 카드 생성 → learning-card API 저장 + E2E 테스트 통과 |
| **Scope** | **In**: Kafka Consumer 설정, note.created 이벤트 처리, learning-card API 연동 / **Out**: 카드 편집 UI, 사용자 승인 플로우 |
| **Input** | Step 5 완료된 플래시카드 생성, note.created Avro 스키마, learning-card API 명세 |
| **Instructions** | 1. Kafka Consumer 설정 (airgap consumer group, Avro Deserializer)<br>2. note.created 이벤트 핸들러 구현<br>3. 이벤트 수신 → 노트 내용 추출 → AI 카드 생성 파이프라인<br>4. 생성된 카드를 learning-card API로 HTTP POST 저장<br>5. 실패 시 재시도 로직 (최대 3회, exponential backoff)<br>6. Dead Letter Queue(DLQ) 설정 (처리 실패 이벤트 보관)<br>7. 통합 테스트: note.created 이벤트 → AI 카드 생성 → 저장 E2E |
| **Output Format** | Kafka Consumer 설정 + 이벤트 핸들러 코드 + E2E 테스트 결과 |
| **Constraints** | - Consumer group: `learning-ai-card-generator`<br>- 이벤트 처리 타임아웃: 60초 (LLM 호출 포함)<br>- 재시도 최대 3회 (2s, 4s, 8s 간격)<br>- DLQ 토픽: `note.created.dlq`<br>- 동일 이벤트 중복 처리 방지 (idempotency key) |
| **Duration** | 2일 |
| **RULE Reference** | [03-아키텍처](../../wiki/03-아키텍처.md) · [18-기술-스택](../../wiki/18-기술-스택.md) · [14-배포-가이드](../../wiki/14-배포-가이드.md) |
| **Assignee** | @learning-ai-owner |
| **Reviewer** | @tech-lead |
| **Status** | TODO |

---

## Step 7: RAG 기반 질문 답변 (P2)

| 필드 | 내용 |
|------|------|
| **Step Name** | RAG 기반 질문 답변 (P2) |
| **Step Goal** | 사용자가 노트 기반으로 AI에게 질문하여 RAG 답변을 받을 수 있다. (시간 허용 시) |
| **Done When** | RAG 파이프라인 구현 + 질문 → 관련 노트 검색 → LLM 답변 생성 + 테스트 통과 |
| **Scope** | **In**: RAG 파이프라인, 질문 API, 컨텍스트 조립, LLM 답변 생성 / **Out**: 대화 히스토리, 스트리밍 응답, 멀티턴 대화 |
| **Input** | Step 4 완료된 시맨틱 검색, Step 2 완료된 Claude API, PRD_W3 RAG 요구사항 |
| **Instructions** | 1. RAG 질문 API 구현 (POST `/api/v1/ai/ask`)<br>2. 질문 텍스트를 벡터로 변환하여 시맨틱 검색 수행<br>3. 검색된 관련 노트 청크를 LLM 컨텍스트로 조립<br>4. Claude API에 컨텍스트 + 질문 전달하여 답변 생성<br>5. 답변에 출처(노트 제목/ID) 포함<br>6. 컨텍스트 윈도우 관리 (최대 토큰 제한)<br>7. pytest 테스트: 질문 → 답변 품질 및 출처 정확도 검증 |
| **Output Format** | RAG API 응답 예시 + 프롬프트 템플릿 + 테스트 결과 |
| **Constraints** | - 우선순위: P2 (시간 허용 시 구현)<br>- 검색 Top-K: 5개 청크<br>- 컨텍스트 최대 토큰: 3000<br>- 답변에 출처 노트 필수 포함<br>- 관련 노트 없을 시 "관련 노트를 찾을 수 없습니다" 응답 |
| **Duration** | 2일 |
| **RULE Reference** | [03-아키텍처](../../wiki/03-아키텍처.md) · [18-기술-스택](../../wiki/18-기술-스택.md) |
| **Assignee** | @learning-ai-owner |
| **Reviewer** | @tech-lead |
| **Status** | TODO |

---

# W4 (2026-06-01 ~ 2026-06-05, 6/3 지방선거 제외 — AI 자동 생성 E2E + 정확도 검증)

## Step 8: AI 카드 자동 생성 E2E 테스트

| 필드 | 내용 |
|------|------|
| **Step Name** | AI 카드 자동 생성 E2E 테스트 |
| **Step Goal** | AI 카드 자동 생성 E2E(노트→이벤트→AI→카드저장) 시나리오가 통과한다. |
| **Done When** | E2E 테스트 시나리오 전체 통과 + CI 연동 + 테스트 리포트 산출 |
| **Scope** | **In**: E2E 테스트 시나리오 작성, CI 연동, 테스트 리포트 / **Out**: 프로덕션 배포, 모니터링 |
| **Input** | Step 6 완료된 자동 카드 생성 파이프라인, CI 파이프라인 설정, E2E 테스트 전략 |
| **Instructions** | 1. E2E 테스트 시나리오 정의 (노트 생성 → Kafka 이벤트 → AI 카드 생성 → learning-card 저장)<br>2. 정상 플로우 E2E 테스트 작성<br>3. LLM 응답 실패 시 재시도 E2E 테스트<br>4. DLQ 전달 시나리오 테스트<br>5. CI 파이프라인에 E2E 테스트 단계 추가<br>6. 테스트 리포트 자동 생성 설정<br>7. 성능 테스트: 이벤트 수신 → 카드 저장 완료 시간 측정 |
| **Output Format** | E2E 테스트 코드 + CI 파이프라인 설정 + 테스트 리포트 |
| **Constraints** | - E2E 테스트 실행 시간 5분 이내<br>- CI에서 E2E 실패 시 빌드 FAIL<br>- Testcontainers로 Kafka/PostgreSQL 구동<br>- LLM 호출은 mock 처리 (비용/시간 절약) |
| **Duration** | 1.5일 |
| **RULE Reference** | [03-아키텍처](../../wiki/03-아키텍처.md) · [09-버전-관리-정책](../../wiki/09-버전-관리-정책.md) · [14-배포-가이드](../../wiki/14-배포-가이드.md) |
| **Assignee** | @learning-ai-owner |
| **Reviewer** | @tech-lead |
| **Status** | TODO |

---

## Step 9: 시맨틱 검색 정확도 검증 및 P0 버그 수정

| 필드 | 내용 |
|------|------|
| **Step Name** | 시맨틱 검색 정확도 검증 및 P0 버그 수정 |
| **Step Goal** | 시맨틱 검색 정확도가 검증되고 P0 버그가 모두 수정된다. |
| **Done When** | 시맨틱 검색 정확도 기준 충족 + P0 버그 0건 + 회귀 테스트 통과 |
| **Scope** | **In**: 시맨틱 검색 정확도 측정, P0 버그 수정, 회귀 테스트 / **Out**: 프로덕션 배포, 신규 기능 추가 |
| **Input** | Step 8 E2E 테스트 결과, P0 버그 목록, 시맨틱 검색 벤치마크 데이터 |
| **Instructions** | 1. 시맨틱 검색 테스트 쿼리 세트 작성 (20건 이상)<br>2. 검색 정확도 측정 (MRR, Precision@10)<br>3. P0 버그 목록 정리 및 우선순위 배정<br>4. 각 P0 버그 원인 분석 및 수정<br>5. 벡터 인덱스 파라미터 튜닝 (HNSW m, ef_construction)<br>6. 회귀 테스트 전체 실행 및 통과 확인<br>7. 최종 검색 품질 보고서 작성 |
| **Output Format** | 정확도 측정 결과 + P0 버그 수정 내역 + 최종 품질 보고서 |
| **Constraints** | - P0 버그 0건 달성 필수<br>- MRR 목표: 0.7 이상<br>- Precision@10 목표: 0.6 이상<br>- 수정으로 인한 기존 테스트 회귀 금지<br>- 코드 프리즈 전 완료 |
| **Duration** | 1.5일 |
| **RULE Reference** | [03-아키텍처](../../wiki/03-아키텍처.md) · [09-버전-관리-정책](../../wiki/09-버전-관리-정책.md) |
| **Assignee** | @learning-ai-owner |
| **Reviewer** | @tech-lead |
| **Status** | TODO |
