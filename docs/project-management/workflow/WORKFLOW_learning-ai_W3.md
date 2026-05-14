# WORKFLOW: @learning-ai-owner — Week 3

> **Task 문서**: [TASK_learning-ai.md](../task/TASK_learning-ai.md)  
> **기간**: 2026-05-26 ~ 2026-05-30  
> **PRD**: [PRD_W3.md](../prd/PRD_W3.md)

---

## Step 6: AI 카드 자동 생성 — note.created Kafka 소비 → LLM → Card 생성 → learning-card API 호출

### 1.1 TASK 시작
- [ ] Step Goal / Done When / Scope / Input 확인
- [ ] PRD_W3 해당 요구사항 확인 (AI 카드 자동 생성)
- [ ] Duration 산정 확인

### 1.2 요구사항 분석
- [ ] note.created Kafka 이벤트 스키마 분석 (noteId, userId, title, content)
- [ ] LLM 프롬프트 설계 (노트 내용 → Q&A 카드 생성, 최대 5장)
- [ ] learning-card API 호출 스펙 분석 (POST /cards bulk create)
- [ ] 생성 실패 시 재시도/DLQ 전략 정의
- [ ] Instructions 초안 → TASK 문서 반영

### 1.3 Security 1차 검토
- [ ] 인증 필요 여부: No (내부 Kafka 소비 + 서비스 간 호출)
- [ ] LLM API 키 관리 (External Secrets)
- [ ] 서비스 간 인증 (ServiceAccount JWT 또는 내부 토큰)
- [ ] 결과 → TASK Constraints 반영

### 1.4 아키텍처 설계
- [ ] 파이프라인 설계 (note.created 소비 → 노트 내용 조회 → LLM 호출 → 카드 생성 API)
- [ ] LLM 모델 선택 (GPT-4o / Claude 등) + 토큰 제한 설정
- [ ] 프롬프트 템플릿 설계 (시스템 프롬프트 + 노트 내용 + 출력 포맷)
- [ ] 비동기 처리 설계 (Kafka 소비 → 별도 스레드풀에서 LLM 호출)
- [ ] Duration(final) 갱신

### 1.5 Security 2차 검토
- [ ] LLM으로 전송되는 노트 내용 민감정보 필터링
- [ ] LLM 응답 검증 (유효한 Q&A 포맷인지 확인)
- [ ] 비용 제어 (사용자당 일일 호출 제한)
- [ ] 결과 → TASK Constraints 반영

### 1.6 DTO / Entity 설계 (API First)
- [ ] NoteCreatedEvent DTO 정의 (Kafka 소비용)
- [ ] LlmCardGenerationRequest DTO 정의 (noteContent, maxCards)
- [ ] LlmCardGenerationResponse DTO 정의 (cards[]: question, answer, difficulty)
- [ ] CardCreateRequest DTO 정의 (learning-card API 호출용)
- [ ] Output Format → TASK 반영

### 1.7 Client 구현
- [ ] NoteApiClient 구현 (knowledge-svc에서 노트 내용 조회)
- [ ] LlmClient 구현 (OpenAI/Anthropic API 호출, 프롬프트 전송, 응답 파싱)
- [ ] CardApiClient 구현 (learning-card-svc POST /cards 호출)

### 1.8 Service + Test
- [ ] AiCardKafkaConsumer 구현 (note.created 토픽 소비)
- [ ] AiCardGenerationService 구현 (노트 조회 → LLM 호출 → 응답 파싱 → 카드 생성)
- [ ] 프롬프트 템플릿 관리 (외부 설정 파일)
- [ ] 실패 시 재시도 (3회) + DLQ 전송
- [ ] 일일 호출 제한 (Redis 카운터, 사용자당 최대 N회)
- [ ] 단위 테스트 작성 (LLM 응답 모킹)
- [ ] 테스트 통과 확인

### 1.9 E2E 검증
- [ ] Docker Compose 환경에서 note.created 이벤트 발행 → 카드 자동 생성 확인
- [ ] 생성된 카드 품질 검수 (Q&A 포맷, 한국어/영어 지원)
- [ ] 실패 시 DLQ 적재 확인
- [ ] LLM 응답 시간 측정 (< 10초 기준)

### 1.10 결과 정리
- [ ] 프롬프트 템플릿 문서화
- [ ] 비용 추정 (일일 예상 LLM API 호출 비용)
- [ ] RULE Reference → TASK 반영

**Step 6 Status**: [ ] Not Started / [ ] In Progress / [ ] Done

---

## Step 7: RAG Q&A (시간 허용 시) — 관련 청크 검색 → LLM 답변 생성 + 시맨틱 캐시

### 1.1 TASK 시작
- [ ] Step Goal / Done When / Scope / Input 확인
- [ ] PRD_W3 해당 요구사항 확인 (RAG Q&A)
- [ ] Duration 산정 확인 (시간 허용 시 진행)

### 1.2 요구사항 분석
- [ ] RAG 파이프라인 분석 (질문 → 관련 청크 검색 → LLM 컨텍스트 주입 → 답변)
- [ ] 시맨틱 캐시 요건 분석 (유사 질문 캐시 히트 → LLM 호출 스킵)
- [ ] 청크 검색 전략 (pgvector 시맨틱 검색, top-K=5)
- [ ] Instructions 초안 → TASK 문서 반영

### 1.3 Security 1차 검토
- [ ] 인증 필요 여부: Yes (로그인 사용자)
- [ ] 권한 종류: 본인 노트 기반 RAG만 허용
- [ ] 청크 검색 시 접근 제어 필터 적용 필수
- [ ] 결과 → TASK Constraints 반영

### 1.4 아키텍처 설계
- [ ] RAG 파이프라인 설계 (질문 임베딩 → pgvector 검색 → 청크 수집 → LLM 프롬프트 구성 → 답변)
- [ ] 시맨틱 캐시 설계 (질문 임베딩 → Redis 유사도 검색, threshold=0.95)
- [ ] 프롬프트 템플릿 설계 (시스템 프롬프트 + 관련 청크 + 질문)
- [ ] Duration(final) 갱신

### 1.5 Security 2차 검토
- [ ] LLM 프롬프트 인젝션 방지 (사용자 질문 새니타이징)
- [ ] 답변에 출처(노트 제목, 링크) 포함
- [ ] 시맨틱 캐시 사용자별 격리
- [ ] 결과 → TASK Constraints 반영

### 1.6 DTO / Entity 설계 (API First)
- [ ] RagQuestionRequest DTO 정의 (question, maxChunks)
- [ ] RagAnswerResponse DTO 정의 (answer, sources[], cached)
- [ ] RagSource DTO 정의 (noteId, noteTitle, chunkText, similarity)
- [ ] SemanticCacheEntry 모델 정의 (questionEmbedding, answer, sources, createdAt)
- [ ] Output Format → TASK 반영

### 1.7 Repository / Client 구현
- [ ] ChunkSearchRepository 구현 (pgvector 유사도 검색 + 사용자 필터)
- [ ] SemanticCacheRepository 구현 (Redis 기반 임베딩 유사도 검색)
- [ ] LlmClient 재사용 (Step 6 구현체)

### 1.8 Service + Test
- [ ] RagService 구현 (질문 → 캐시 확인 → 청크 검색 → LLM 호출 → 답변 생성)
- [ ] SemanticCacheService 구현 (유사 질문 검색, 캐시 저장, TTL 관리)
- [ ] 청크 수집 로직 (top-K=5, 최소 유사도 threshold=0.7)
- [ ] 프롬프트 구성 로직 (청크 컨텍스트 + 질문 + 출처 포맷 지시)
- [ ] 단위 테스트 작성 (LLM 응답 모킹, 캐시 히트/미스 시나리오)
- [ ] 테스트 통과 확인

### 1.9 Controller + Test
- [ ] POST /ai/ask 엔드포인트 구현 (RAG Q&A)
- [ ] 응답에 출처 정보 포함 (노트 제목 + 관련도)
- [ ] 슬라이스 테스트 (@WebMvcTest)
- [ ] 캐시 히트 시 빠른 응답 확인
- [ ] 테스트 통과 확인

### 1.10 결과 정리
- [ ] RAG 파이프라인 아키텍처 문서화
- [ ] 시맨틱 캐시 히트율 측정 방안
- [ ] 답변 품질 평가 기준 정의
- [ ] RULE Reference → TASK 반영

**Step 7 Status**: [ ] Not Started / [ ] In Progress / [ ] Done
