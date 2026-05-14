# WORKFLOW: @learning-card-owner — Week 3

> **Task 문서**: [TASK_learning-card.md](../task/TASK_learning-card.md)  
> **기간**: 2026-05-26 ~ 2026-05-30  
> **PRD**: [PRD_W3.md](../prd/PRD_W3.md)

---

## Step 7: card.review.due 발행 — 매일 스케줄러 → 복습 대상 사용자 → Kafka 발행

### 1.1 TASK 시작
- [ ] Step Goal / Done When / Scope / Input 확인
- [ ] PRD_W3 해당 요구사항 확인 (복습 알림 Kafka 발행)
- [ ] Duration 산정 확인

### 1.2 요구사항 분석
- [ ] 스케줄러 실행 시간 정의 (매일 오전 8시 KST)
- [ ] 복습 대상 판정 기준 분석 (SM-2 알고리즘 next_review_date ≤ today)
- [ ] card.review.due 이벤트 스키마 정의 (userId, cardCount, dueDate)
- [ ] Instructions 초안 → TASK 문서 반영

### 1.3 Security 1차 검토
- [ ] 인증 필요 여부: No (내부 스케줄러, API 미노출)
- [ ] Kafka 토픽 ACL: learning-card-svc만 발행 권한
- [ ] 스케줄러 중복 실행 방지 (ShedLock 등)
- [ ] 결과 → TASK Constraints 반영

### 1.4 Kafka 토픽 설계
- [ ] card.review.due 토픽 설정 (파티션, 복제, 보존)
- [ ] 이벤트 키 전략 (userId 기반 파티셔닝)
- [ ] Schema Registry 스키마 등록
- [ ] Duration(final) 갱신

### 1.5 Security 2차 검토
- [ ] 이벤트 페이로드 민감정보 미포함 확인 (카드 내용 X, 건수만)
- [ ] 스케줄러 실패 시 재시도 정책 (최대 3회)
- [ ] 대량 사용자 처리 시 배치 크기 제한
- [ ] 결과 → TASK Constraints 반영

### 1.6 DTO / Entity 설계 (API First)
- [ ] CardReviewDueEvent DTO 정의 (userId, cardCount, dueDate, occurredAt)
- [ ] Avro/JSON Schema 작성 → Schema Registry 등록
- [ ] Output Format → TASK 반영

### 1.7 Producer 구현
- [ ] ReviewDueKafkaProducer 구현 (KafkaTemplate)
- [ ] publishReviewDue(userId, cardCount) 메서드 구현
- [ ] 멱등성 Producer 설정 (enable.idempotence=true)
- [ ] 배치 발행 최적화 (사용자별 개별 이벤트)

### 1.8 Service + Test
- [ ] ReviewDueScheduler 구현 (@Scheduled cron = "0 0 8 * * *" Asia/Seoul)
- [ ] ShedLock 설정 (중복 실행 방지, lockAtMostFor=30m)
- [ ] 복습 대상 사용자 조회 쿼리 (next_review_date ≤ today, GROUP BY user_id)
- [ ] 사용자별 card.review.due 이벤트 발행
- [ ] 통합 테스트 작성 (EmbeddedKafka + 스케줄러 트리거)
- [ ] 테스트 통과 확인

### 1.9 E2E 검증
- [ ] Docker Compose 환경에서 스케줄러 수동 트리거
- [ ] kafka-console-consumer로 card.review.due 이벤트 수신 확인
- [ ] notification 서비스 연동 테스트 (이벤트 → 알림 발송)
- [ ] 대량 사용자 시뮬레이션 (1000명 기준 처리 시간 측정)

### 1.10 결과 정리
- [ ] 이벤트 스키마 문서화
- [ ] 스케줄러 모니터링 방안 (실행 로그, 실패 알림)
- [ ] RULE Reference → TASK 반영

**Step 7 Status**: [ ] Not Started / [ ] In Progress / [ ] Done

---

## Step 8: 복습 통계 대시보드 — 일별/주별 복습 수, 정답률, 스트릭 통합 API

### 1.1 TASK 시작
- [ ] Step Goal / Done When / Scope / Input 확인
- [ ] PRD_W3 해당 요구사항 확인 (복습 통계 대시보드)
- [ ] Duration 산정 확인

### 1.2 요구사항 분석
- [ ] 일별 복습 통계 요건 (복습 수, 정답률)
- [ ] 주별 복습 통계 요건 (주간 합산, 추세 그래프 데이터)
- [ ] 스트릭 통합 요건 (engagement-svc user_streaks 연동)
- [ ] Instructions 초안 → TASK 문서 반영

### 1.3 Security 1차 검토
- [ ] 인증 필요 여부: Yes (로그인 사용자)
- [ ] 권한 종류: 본인 통계만 조회 가능
- [ ] 공개 API 여부: No
- [ ] 결과 → TASK Constraints 반영

### 1.4 데이터 모델 설계
- [ ] 일별 통계 집계 방식 결정 (실시간 집계 vs 사전 집계 테이블)
- [ ] review_daily_stats 테이블 설계 (user_id, date, review_count, correct_count, total_count)
- [ ] 인덱스 설계 (user_id + date)
- [ ] Duration(final) 갱신

### 1.5 Security 2차 검토
- [ ] 타인 통계 조회 차단 (user_id 필터 필수)
- [ ] 통계 데이터 민감정보 미포함
- [ ] API 응답 캐싱 (Redis TTL: 5분)
- [ ] 결과 → TASK Constraints 반영

### 1.6 DTO / Entity 설계 (API First)
- [ ] ReviewDailyStat Entity 작성 (또는 집계 쿼리 방식 결정)
- [ ] DailyReviewStatResponse DTO 정의 (date, reviewCount, correctRate)
- [ ] WeeklyReviewStatResponse DTO 정의 (weekStart, totalReviews, avgCorrectRate, dailyBreakdown[])
- [ ] ReviewDashboardResponse DTO 정의 (daily, weekly, streak 통합)
- [ ] Output Format → TASK 반영

### 1.7 Repository 구현
- [ ] ReviewStatRepository 구현 (일별/주별 집계 쿼리)
- [ ] 일별 통계: GROUP BY date, COUNT(*), AVG(correct)
- [ ] 주별 통계: 최근 4주 집계
- [ ] engagement-svc 연동 (REST 또는 Kafka로 스트릭 조회)

### 1.8 Service + Test
- [ ] ReviewStatService 구현 (일별 통계 조회)
- [ ] ReviewStatService 구현 (주별 통계 조회 — 최근 4주)
- [ ] ReviewDashboardService 구현 (일별 + 주별 + 스트릭 통합)
- [ ] 스트릭 데이터 연동 (engagement-svc REST 호출 + fallback)
- [ ] Redis 캐싱 적용 (TTL: 5분)
- [ ] 단위 테스트 작성 (Mockito)
- [ ] 테스트 통과 확인

### 1.9 Controller + Test
- [ ] GET /reviews/stats/daily?from=&to= 엔드포인트 구현
- [ ] GET /reviews/stats/weekly 엔드포인트 구현 (최근 4주)
- [ ] GET /reviews/dashboard 엔드포인트 구현 (통합 대시보드)
- [ ] 슬라이스 테스트 (@WebMvcTest)
- [ ] 빈 데이터, 기간 필터 테스트
- [ ] 테스트 통과 확인

### 1.10 View + Test (해당 시)
- [ ] Flutter 화면 연동: 해당 없음 (프론트 별도)
- [ ] Swagger API 문서 확인
- [ ] RULE Reference → TASK 반영

**Step 8 Status**: [ ] Not Started / [ ] In Progress / [ ] Done
