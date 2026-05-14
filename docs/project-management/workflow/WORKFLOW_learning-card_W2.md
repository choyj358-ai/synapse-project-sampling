# WORKFLOW: @learning-card-owner — Week 2

> **Task 문서**: [TASK_learning-card.md](../task/TASK_learning-card.md)  
> **기간**: 2026-05-19 ~ 2026-05-23  
> **PRD**: [PRD_W2.md](../prd/PRD_W2.md)

---

## Step 4: SRS 복습 세션 완성 — 오늘 복습 카드 큐 + rating → SM-2 → 다음 복습일

### 4.1 TASK 시작
- [ ] Step Goal / Done When / Scope / Input 확인
- [ ] PRD_W2 해당 요구사항 확인 (SRS 복습 세션)
- [ ] Duration 산정 확인

### 4.2 요구사항 분석
- [ ] 오늘 복습 대상 카드 큐 조회 로직 정의 (nextReviewDate <= today)
- [ ] 복습 세션 생성/완료 플로우 정의
- [ ] rating 제출 → SM-2 → 다음 복습일 계산 플로우 확인
- [ ] 세션 내 카드 순서 정책 (오래된 순 / 랜덤)
- [ ] Instructions 초안 → TASK 문서 반영

### 4.3 Security 1차 검토
- [ ] 인증 필요 여부: Yes (JWT 인증 필요)
- [ ] 권한 종류: 로그인 사용자 (본인 카드만 복습)
- [ ] 공개 API 여부: No
- [ ] 결과 → TASK Constraints 반영

### 4.4 ERD 설계
- [ ] review_sessions 테이블 설계 (id, userId, startedAt, completedAt, totalCards, reviewedCards, createdAt)
- [ ] cards 테이블 갱신 확인 (nextReviewDate, interval, easeFactor 컬럼)
- [ ] 인덱스 설계 (review_sessions.userId, cards.userId+nextReviewDate)
- [ ] 관계 정의 (review_sessions.userId → users.id)
- [ ] Duration(final) 갱신

### 4.5 Security 2차 검토
- [ ] 세션 데이터 접근 제어 (본인 세션만)
- [ ] Soft Delete 정책: 물리삭제 없음 (세션 이력 보관)
- [ ] 행 단위 접근 제어: 필요 (userId 기반)
- [ ] 결과 → TASK Constraints 반영

### 4.6 DTO / Entity 설계 (API First)
- [ ] ReviewSessionResponse 정의 (id, totalCards, reviewedCards, startedAt, completedAt)
- [ ] ReviewQueueResponse 정의 (cards[], totalCount)
- [ ] ReviewCardResponse 정의 (cardId, front, back, deckTitle)
- [ ] ReviewSubmitRequest 정의 (cardId, rating)
- [ ] ReviewSession Entity 작성
- [ ] MapStruct 매퍼 작성
- [ ] Output Format → TASK 반영

### 4.7 Repository 구현
- [ ] ReviewSessionRepository 인터페이스 작성
- [ ] findByUserIdOrderByStartedAtDesc 커스텀 쿼리
- [ ] findDueCards 쿼리 (nextReviewDate <= today AND userId = ?)
- [ ] Flyway 마이그레이션 스크립트 작성

### 4.8 Service + Test
- [ ] ReviewSessionService 구현 (startSession, submitReview, completeSession, getQueue)
- [ ] 오늘 복습 카드 큐 조회 서비스 (due cards)
- [ ] 세션 시작 로직 (totalCards 스냅샷)
- [ ] rating 제출 → SM-2 호출 → nextReviewDate 갱신
- [ ] 세션 완료 로직 (completedAt + reviewedCards 갱신)
- [ ] Bean Validation 적용
- [ ] 단위 테스트 작성 (Mockito)
- [ ] 통합 테스트 (세션 시작 → 복습 → 완료 플로우)
- [ ] 테스트 통과 확인

### 4.9 Controller + Test
- [ ] POST /api/v1/review/sessions 엔드포인트 구현 (세션 시작)
- [ ] GET /api/v1/review/sessions/{id}/queue 엔드포인트 구현 (카드 큐)
- [ ] POST /api/v1/review/sessions/{id}/submit 엔드포인트 구현 (rating 제출)
- [ ] POST /api/v1/review/sessions/{id}/complete 엔드포인트 구현 (세션 완료)
- [ ] 슬라이스 테스트 (@WebMvcTest)
- [ ] 401/403 응답 테스트
- [ ] 통합 테스트 (전체 복습 플로우)
- [ ] 테스트 통과 확인

### 4.10 View + Test (해당 시)
- [ ] Flutter 화면 연동: 해당 없음 (프론트 별도)
- [ ] Swagger API 문서 확인
- [ ] RULE Reference → TASK 반영

**Step 4 Status**: [ ] Not Started / [ ] In Progress / [ ] Done

---

## Step 5: card.reviewed Kafka 발행 — Avro 스키마 + 이벤트 발행

### 5.1 TASK 시작
- [ ] Step Goal / Done When / Scope / Input 확인
- [ ] PRD_W2 해당 요구사항 확인 (card.reviewed Kafka 발행)
- [ ] Duration 산정 확인

### 5.2 요구사항 분석
- [ ] card.reviewed 이벤트 페이로드 정의 (userId, cardId, deckId, rating, reviewedAt)
- [ ] Avro 스키마 필드 타입 정의
- [ ] Schema Registry 등록 절차 확인
- [ ] 이벤트 발행 시점 정의 (rating 제출 직후)
- [ ] Instructions 초안 → TASK 문서 반영

### 5.3 Security 1차 검토
- [ ] Kafka 프로듀서 인증 설정 확인
- [ ] 메시지 무결성 보장 (Avro 직렬화)
- [ ] 민감 정보 포함 여부 확인 (userId만, 개인정보 미포함)
- [ ] 결과 → TASK Constraints 반영

### 5.4 ERD 설계
- [ ] ERD 해당 없음 (이벤트 발행 — DB 변경 없음)
- [ ] Avro 스키마 파일 설계 (.avsc)
- [ ] Duration(final) 갱신

### 5.5 Security 2차 검토
- [ ] Schema Registry BACKWARD 호환성 검증 확인
- [ ] 메시지 암호화 전송 (TLS) 확인
- [ ] 결과 → TASK Constraints 반영

### 5.6 DTO / Entity 설계 (API First)
- [ ] CardReviewedEvent Avro 스키마 정의 (card_reviewed.avsc)
- [ ] 필드 정의: userId(string), cardId(string), deckId(string), rating(enum), reviewedAt(timestamp-millis)
- [ ] default 값 설정 (BACKWARD 호환성)
- [ ] Output Format → TASK 반영

### 5.7 Repository 구현
- [ ] Avro 스키마 파일 작성 (src/main/avro/card_reviewed.avsc)
- [ ] Avro 코드 생성 플러그인 설정 (Gradle avro plugin)
- [ ] Schema Registry에 스키마 등록 (POST /subjects/card.reviewed-value/versions)

### 5.8 Service + Test
- [ ] CardReviewedEventPublisher 구현 (KafkaTemplate + Avro 직렬화)
- [ ] ReviewSessionService에 이벤트 발행 로직 추가 (rating 제출 후)
- [ ] 이벤트 발행 실패 시 재시도/로깅 처리
- [ ] 트랜잭션 아웃박스 패턴 검토 (선택)
- [ ] 단위 테스트 작성 (Mockito — KafkaTemplate mock)
- [ ] Kafka 발행 테스트 (@EmbeddedKafka)
- [ ] 테스트 통과 확인

### 5.9 Controller + Test
- [ ] Controller 해당 없음 (내부 이벤트 발행)
- [ ] 통합 테스트 (rating 제출 → Kafka 메시지 발행 확인)
- [ ] Schema Registry 호환성 테스트 (비호환 스키마 → 거부)
- [ ] 테스트 통과 확인

### 5.10 View + Test (해당 시)
- [ ] Flutter 화면 연동: 해당 없음
- [ ] Kafka 토픽 메시지 확인 (kafka-console-consumer)
- [ ] RULE Reference → TASK 반영

**Step 5 Status**: [ ] Not Started / [ ] In Progress / [ ] Done

---

## Step 6: review_sessions 통계 — 일별/주별 복습 수, 정답률 API

### 6.1 TASK 시작
- [ ] Step Goal / Done When / Scope / Input 확인
- [ ] PRD_W2 해당 요구사항 확인 (복습 통계)
- [ ] Duration 산정 확인

### 6.2 요구사항 분석
- [ ] 일별 복습 수 집계 요건 (최근 N일)
- [ ] 주별 복습 수 집계 요건 (최근 N주)
- [ ] 정답률 계산 기준 정의 (Good+Easy / 전체)
- [ ] 통계 데이터 응답 포맷 정의
- [ ] Instructions 초안 → TASK 문서 반영

### 6.3 Security 1차 검토
- [ ] 인증 필요 여부: Yes (JWT 인증 필요)
- [ ] 권한 종류: 로그인 사용자 (본인 통계만)
- [ ] 공개 API 여부: No
- [ ] 결과 → TASK Constraints 반영

### 6.4 ERD 설계
- [ ] 추가 테이블 불필요 (review_logs + review_sessions 기반 집계)
- [ ] 집계 쿼리 설계 (GROUP BY date/week, COUNT, 정답률 계산)
- [ ] 인덱스 최적화 확인 (review_logs.userId + reviewedAt)
- [ ] Duration(final) 갱신

### 6.5 Security 2차 검토
- [ ] 통계 데이터 접근 제어 (본인만)
- [ ] 다른 사용자 통계 접근 불가 확인
- [ ] 결과 → TASK Constraints 반영

### 6.6 DTO / Entity 설계 (API First)
- [ ] DailyReviewStatResponse 정의 (date, reviewCount, correctRate)
- [ ] WeeklyReviewStatResponse 정의 (weekStart, reviewCount, correctRate)
- [ ] ReviewStatsResponse 정의 (daily[], weekly[], totalReviews, overallCorrectRate)
- [ ] Output Format → TASK 반영

### 6.7 Repository 구현
- [ ] ReviewLogRepository 커스텀 쿼리 추가
- [ ] 일별 집계 쿼리 (GROUP BY DATE(reviewedAt), COUNT, 정답률)
- [ ] 주별 집계 쿼리 (GROUP BY WEEK, COUNT, 정답률)
- [ ] Native Query 또는 JPQL 작성

### 6.8 Service + Test
- [ ] ReviewStatsService 구현 (getDailyStats, getWeeklyStats, getOverallStats)
- [ ] 일별 복습 수 + 정답률 계산 로직
- [ ] 주별 복습 수 + 정답률 계산 로직
- [ ] 기간 필터링 (최근 30일 / 12주)
- [ ] 데이터 없는 날짜 0으로 채우기 로직
- [ ] 단위 테스트 작성 (Mockito)
- [ ] 경계값 테스트 (데이터 없는 기간, 정답률 0%)
- [ ] 테스트 통과 확인

### 6.9 Controller + Test
- [ ] GET /api/v1/review/stats/daily?days={n} 엔드포인트 구현
- [ ] GET /api/v1/review/stats/weekly?weeks={n} 엔드포인트 구현
- [ ] GET /api/v1/review/stats 엔드포인트 구현 (종합)
- [ ] 슬라이스 테스트 (@WebMvcTest)
- [ ] 401/403 응답 테스트
- [ ] 통합 테스트 (복습 데이터 → 통계 API 검증)
- [ ] 테스트 통과 확인

### 6.10 View + Test (해당 시)
- [ ] Flutter 화면 연동: 해당 없음 (프론트 별도)
- [ ] Swagger API 문서 확인
- [ ] RULE Reference → TASK 반영

**Step 6 Status**: [ ] Not Started / [ ] In Progress / [ ] Done
