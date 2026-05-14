# 작업 스코프: @learning-ai-owner

## 담당자 정보

| 항목 | 내용 |
|------|------|
| Handle | @learning-ai-owner |
| 역할 | 트랙 D-2 (2명 중 1명) |
| 담당 서비스 | synapse-learning-svc / learning-ai |
| 담당 모듈 | ai (Python / FastAPI) |
| GitHub Repository | [synapse-learning-svc](https://github.com/team-project-final/synapse-learning-svc) (`learning-ai` 모듈) |

## 4주 전체 책임 범위

### 도메인 경계

- **In Scope**:
  - FastAPI 서비스 scaffolding + Dockerfile
  - Anthropic Claude API 연동 (카드 생성용 LLM 호출)
  - OpenAI Embedding API 연결 (텍스트 → 벡터 1536차원)
  - pgvector 임베딩 저장/조회
  - 시맨틱 검색 (코사인 유사도 쿼리)
  - AI 카드 자동 생성 (note.created 이벤트 소비 → LLM → Card 생성 요청)
  - RAG Q&A (시간 허용 시: 노트 기반 질의응답)
  - 시맨틱 캐시 (코사인 유사도 > 0.95 캐시 히트)
- **Out of Scope**:
  - 카드/덱 CRUD Java 로직 (learning-card-owner 담당)
  - K8s 인프라 (team-lead 담당)
  - BM25 키워드 검색 (knowledge-owner-2 담당)

### 주차별 스코프 매트릭스

| 주차 | 기간 | 핵심 목표 | 산출물 | 의존성 |
|------|------|-----------|--------|--------|
| W1 | 05-12~16 | FastAPI 골격 + Claude API + Embedding API | 서비스 골격, /generate API, /embed API | 인프라 (team-lead) |
| W2 | 05-19~23 | 시맨틱 검색 골격 + AI 카드 생성 골격 | pgvector 조회 API, AI 카드 프로토타입 | note CRUD (knowledge-owner-1 W1) |
| W3 | 05-26~30 | AI 카드 자동 생성 (note.created 소비) + RAG (시간 허용 시) | Kafka 소비자, 자동 카드 생성, RAG API | Kafka (team-lead W2) |
| W4 | 06-02~06 | 버그 수정 + 통합 테스트 | 안정화 | 전체 통합 (W3) |

## 협업 인터페이스

| 상대 | 주고받는 것 | 방향 |
|------|------------|------|
| @knowledge-owner-1 | note.created Kafka 이벤트 | ← 수신 |
| @knowledge-owner-2 | 시맨틱 벡터 (pgvector) 제공 | 제공 → |
| @learning-card-owner | AI 생성 카드 → card API 호출로 저장 | 요청 → |
| @team-lead | Kafka 토픽/스키마 협의 | 양방향 |

## 성공 기준

- [ ] FastAPI 서비스 Docker 실행 + Health OK
- [ ] Claude API 호출 → 텍스트 생성 동작
- [ ] Embedding API → 벡터 변환 + pgvector 저장
- [ ] 시맨틱 검색 (유사도 기반 노트 검색)
- [ ] AI 카드 자동 생성 (노트 → LLM → 카드)
