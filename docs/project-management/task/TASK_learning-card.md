# TASK: @learning-card-owner

> **담당 서비스**: learning-card-svc
> **GitHub Repository**: [synapse-learning-svc](https://github.com/team-project-final/synapse-learning-svc) (`learning-card` 모듈)
> **주차**: W1 (2026-05-12 ~ 2026-05-16)
> **관련 문서**: [SCOPE](../scope/SCOPE_learning-card.md) | [PRD_W1](../prd/PRD_W1.md) | [WORKFLOW](../workflow/WORKFLOW_learning-card_W1.md) | [HISTORY](../history/HISTORY_learning-card.md)

---

## Step 1: 프로젝트 초기 설정

| 필드 | 내용 |
|------|------|
| **Step Name** | 프로젝트 초기 설정 |
| **Step Goal** | learning-card-owner가 Spring Boot 4 + Modulith 기반 learning-card 프로젝트를 생성하여 card/srs 모듈 골격이 동작한다. |
| **Done When** | 빌드 성공 + Modulith verify + Health endpoint |
| **Scope** | **In**: Spring Boot 4 프로젝트 생성, Modulith 설정, card/srs 모듈 패키지, Health endpoint / **Out**: 비즈니스 로직, DB 마이그레이션, API 구현 |
| **Input** | Spring Initializr 설정, PRD_W1 모듈 구조 요구사항, 팀 공통 Gradle 설정 |
| **Instructions** | 1. Spring Initializr로 Spring Boot 4 + Java 21 프로젝트 생성<br>2. `build.gradle.kts`에 Modulith, Actuator, Web 의존성 추가<br>3. `card`, `srs` 패키지 생성 및 `@ApplicationModule` 설정<br>4. `ApplicationModules.verify()` 테스트 작성<br>5. Health endpoint (`/actuator/health`) 활성화 및 확인<br>6. `.gitignore`, `Dockerfile`, `docker-compose.yml` 기본 설정 |
| **Output Format** | 프로젝트 디렉토리 구조 + 빌드 로그 + Health 응답 캡처 |
| **Constraints** | - Spring Boot 4.x + Java 21<br>- Gradle Kotlin DSL 사용<br>- 포트: 8082<br>- Modulith verify 필수 통과 |
| **Duration** | 0.5일 |
| **RULE Reference** | [18-기술-스택](../../wiki/18-기술-스택.md) · [10-환경-설정](../../wiki/10-환경-설정.md) |
| **Assignee** | @learning-card-owner |
| **Reviewer** | @tech-lead |

---

## Step 2: 덱/카드 CRUD API

| 필드 | 내용 |
|------|------|
| **Step Name** | 덱/카드 CRUD API |
| **Step Goal** | 로그인 사용자가 덱(Deck)을 생성/관리하고, 덱 내 카드(앞면/뒷면)를 생성/조회/수정/삭제할 수 있다. |
| **Done When** | 덱/카드 CRUD API + 1:N 관계 + 테스트 통과 |
| **Scope** | **In**: decks 테이블, cards 테이블, CRUD API, 페이지네이션 / **Out**: SM-2 알고리즘, 복습 세션, Kafka 이벤트 |
| **Input** | Step 1 완료된 프로젝트, ERD 설계, PRD_W1 API 명세 |
| **Instructions** | 1. `decks` 테이블 스키마 설계 (id, userId, title, description, createdAt, updatedAt)<br>2. `cards` 테이블 스키마 설계 (id, deckId, front, back, createdAt, updatedAt)<br>3. Flyway 마이그레이션 스크립트 작성<br>4. Deck CRUD REST API 구현 (POST/GET/PUT/DELETE)<br>5. Card CRUD REST API 구현 (Deck 하위 리소스)<br>6. 페이지네이션 적용 (Pageable, 기본 20건)<br>7. 통합 테스트 작성 (각 API 엔드포인트별)<br>8. 입력값 검증 (Bean Validation) 적용 |
| **Output Format** | API 엔드포인트 목록 + 테스트 결과 + Flyway 마이그레이션 파일 |
| **Constraints** | - RESTful URL 규칙 준수 (`/api/v1/decks/{deckId}/cards`)<br>- userId는 JWT에서 추출 (인증 필터 연동)<br>- 페이지네이션 최대 100건 제한<br>- soft delete 적용 (deletedAt 컬럼)<br>- PostgreSQL 사용 |
| **Duration** | 2일 |
| **RULE Reference** | [03-아키텍처](../../wiki/03-아키텍처.md) · [18-기술-스택](../../wiki/18-기술-스택.md) · [09-버전-관리-정책](../../wiki/09-버전-관리-정책.md) |
| **Assignee** | @learning-card-owner |
| **Reviewer** | @tech-lead |

---

## Step 3: SM-2 알고리즘 구현

| 필드 | 내용 |
|------|------|
| **Step Name** | SM-2 알고리즘 구현 |
| **Step Goal** | 시스템이 카드 복습 결과(rating)를 받아 SM-2 알고리즘으로 다음 복습일과 ease factor를 계산한다. |
| **Done When** | SM-2 계산 로직 + 단위 테스트(4개 rating × 경계값) 통과 |
| **Scope** | **In**: SM-2 알고리즘 로직, rating 입력 처리, interval/EF 계산 / **Out**: 복습 세션 UI, 복습 스케줄링, 통계 집계 |
| **Input** | SM-2 알고리즘 논문/명세, Step 2 완료된 카드 엔티티, PRD_W1 SRS 요구사항 |
| **Instructions** | 1. `Sm2Calculator` 도메인 서비스 클래스 생성<br>2. rating enum 정의 (Again=0, Hard=1, Good=2, Easy=3)<br>3. interval 계산 로직 구현 (rating별 분기)<br>4. ease factor 업데이트 로직 구현 (최소 1.3 보장)<br>5. `review_logs` 테이블 설계 및 마이그레이션<br>6. 단위 테스트 작성: 4개 rating × 초기/중간/고EF 경계값<br>7. 통합 테스트: POST `/api/v1/cards/{cardId}/review` 엔드포인트 |
| **Output Format** | SM-2 계산 클래스 + 단위 테스트 결과 + review API 응답 예시 |
| **Constraints** | - Again → interval = 1 (리셋)<br>- Hard → interval 유지 (변경 없음)<br>- Good → interval × EF<br>- Easy → interval × EF × 2<br>- EF 최솟값: 1.3<br>- 초기 interval: 1일<br>- 부동소수점 연산 시 반올림 처리 |
| **Duration** | 1.5일 |
| **RULE Reference** | [03-아키텍처](../../wiki/03-아키텍처.md) · [18-기술-스택](../../wiki/18-기술-스택.md) |
| **Assignee** | @learning-card-owner |
| **Reviewer** | @tech-lead |

---

# W2 (2026-05-19 ~ 2026-05-23)

## Step 4: 복습 세션 및 SM-2 스케줄링

| 필드 | 내용 |
|------|------|
| **Step Name** | 복습 세션 및 SM-2 스케줄링 |
| **Step Goal** | 사용자가 복습 세션을 시작하여 오늘 복습할 카드를 받고, 난이도를 매기면 SM-2로 다음 복습일이 계산된다. |
| **Done When** | 복습 세션 시작 API + 오늘 복습 대상 카드 조회 + rating 제출 → SM-2 계산 + 테스트 통과 |
| **Scope** | **In**: review_sessions 테이블, 복습 큐, rating API / **Out**: Kafka 발행(Step 5) |
| **Input** | Step 3 완료된 SM-2 알고리즘, 카드 엔티티, PRD_W2 복습 세션 요구사항 |
| **Instructions** | 1. `review_sessions` 테이블 스키마 설계 (id, userId, startedAt, completedAt, cardCount)<br>2. Flyway 마이그레이션 스크립트 작성<br>3. 복습 세션 시작 API 구현 (POST `/api/v1/review-sessions`)<br>4. 오늘 복습 대상 카드 조회 로직 구현 (nextReviewDate ≤ today)<br>5. 카드 rating 제출 API 구현 (POST `/api/v1/review-sessions/{id}/cards/{cardId}/rate`)<br>6. SM-2 알고리즘 연동하여 다음 복습일 계산 및 저장<br>7. 통합 테스트: 세션 시작 → 카드 조회 → rating → 다음 복습일 검증 |
| **Output Format** | review_sessions DDL + 복습 API 엔드포인트 목록 + 테스트 결과 |
| **Constraints** | - 복습 대상: nextReviewDate ≤ 오늘 날짜인 카드<br>- 세션당 최대 50장 제한<br>- rating 범위: 0(Again) ~ 3(Easy)<br>- SM-2 계산 후 즉시 nextReviewDate 업데이트<br>- PostgreSQL 사용 |
| **Duration** | 2일 |
| **RULE Reference** | [03-아키텍처](../../wiki/03-아키텍처.md) · [18-기술-스택](../../wiki/18-기술-스택.md) |
| **Assignee** | @learning-card-owner |
| **Reviewer** | @tech-lead |
| **Status** | TODO |

---

## Step 5: card.reviewed Kafka 이벤트 발행

| 필드 | 내용 |
|------|------|
| **Step Name** | card.reviewed Kafka 이벤트 발행 |
| **Step Goal** | 복습 완료 시 card.reviewed Kafka 이벤트(Avro)가 Schema Registry에 등록된 스키마로 발행된다. |
| **Done When** | card.reviewed 이벤트 Avro 스키마 등록 + 복습 완료 시 Kafka 발행 + 컨슈머 테스트 통과 |
| **Scope** | **In**: card.reviewed Avro 스키마, Kafka Producer 설정, Schema Registry 연동 / **Out**: 이벤트 소비 서비스 구현, 통계 집계 |
| **Input** | Step 4 완료된 복습 세션, Kafka/Schema Registry 설정, PRD_W2 이벤트 명세 |
| **Instructions** | 1. `card-reviewed-v1.avsc` Avro 스키마 파일 작성 (cardId, userId, rating, reviewedAt)<br>2. Schema Registry에 스키마 등록<br>3. Kafka Producer 설정 (Spring Kafka + Avro Serializer)<br>4. 복습 완료 이벤트 발행 로직 구현 (rating 제출 후 비동기 발행)<br>5. Kafka 토픽 `card.reviewed` 생성 설정<br>6. 컨슈머 통합 테스트 작성 (이벤트 수신 확인)<br>7. 발행 실패 시 재시도 로직 구현 |
| **Output Format** | Avro 스키마 파일 + Kafka Producer 설정 + 이벤트 발행/수신 테스트 결과 |
| **Constraints** | - Avro 직렬화 필수<br>- Schema Registry BACKWARD 호환성<br>- 발행 실패 시 최대 3회 재시도<br>- 이벤트 순서 보장 (userId 기반 파티션 키)<br>- 이벤트 발행은 비동기 (복습 응답 지연 금지) |
| **Duration** | 0.5일 |
| **RULE Reference** | [03-아키텍처](../../wiki/03-아키텍처.md) · [18-기술-스택](../../wiki/18-기술-스택.md) · [14-배포-가이드](../../wiki/14-배포-가이드.md) |
| **Assignee** | @learning-card-owner |
| **Reviewer** | @tech-lead |
| **Status** | TODO |

---

## Step 6: 일별/주별 복습 통계 조회

| 필드 | 내용 |
|------|------|
| **Step Name** | 일별/주별 복습 통계 조회 |
| **Step Goal** | 사용자가 일별/주별 복습 수와 정답률 통계를 조회할 수 있다. |
| **Done When** | 일별/주별 통계 API + 정답률 계산 + 테스트 통과 |
| **Scope** | **In**: 복습 로그 기반 통계 집계, 일별/주별 API / **Out**: 대시보드 UI, 스트릭 계산, XP 연동 |
| **Input** | Step 4-5 완료된 복습 로그 데이터, PRD_W2 통계 요구사항 |
| **Instructions** | 1. 일별 복습 통계 API 구현 (GET `/api/v1/stats/daily`)<br>2. 주별 복습 통계 API 구현 (GET `/api/v1/stats/weekly`)<br>3. 정답률 계산 로직 구현 (Good+Easy / 전체 rating)<br>4. 날짜 범위 필터 파라미터 지원<br>5. 통계 응답 DTO 설계 (날짜, 복습 수, 정답률)<br>6. 캐싱 적용 (일별 통계는 당일 만료)<br>7. 통합 테스트: 복습 데이터 기반 통계 정확도 검증 |
| **Output Format** | 통계 API 엔드포인트 + 응답 JSON 예시 + 테스트 결과 |
| **Constraints** | - 정답률: (Good + Easy 횟수) / 전체 복습 횟수 × 100<br>- 날짜 범위 최대 90일<br>- 빈 날짜는 0으로 채움<br>- 통계 조회 응답 시간 500ms 이내<br>- PostgreSQL 집계 쿼리 사용 |
| **Duration** | 0.5일 |
| **RULE Reference** | [03-아키텍처](../../wiki/03-아키텍처.md) · [18-기술-스택](../../wiki/18-기술-스택.md) |
| **Assignee** | @learning-card-owner |
| **Reviewer** | @tech-lead |
| **Status** | TODO |

---

# W3 (2026-05-26 ~ 2026-05-29, 5/25 부처님오신날 제외 — card.review.due 발행 + 통계 대시보드)

## Step 7: 복습 리마인더 Kafka 이벤트 발행

| 필드 | 내용 |
|------|------|
| **Step Name** | 복습 리마인더 Kafka 이벤트 발행 |
| **Step Goal** | 시스템이 매일 복습 대상 카드가 있는 사용자에게 card.review.due Kafka 이벤트를 발행한다. |
| **Done When** | 스케줄러 실행 + 복습 대상 사용자 추출 + card.review.due 이벤트 발행 + 테스트 통과 |
| **Scope** | **In**: 일일 스케줄러, 복습 대상 사용자 추출, card.review.due 이벤트 발행 / **Out**: 알림 UI, 푸시 알림, 이메일 발송 |
| **Input** | Step 5 완료된 Kafka 설정, 복습 스케줄 데이터, PRD_W3 리마인더 요구사항 |
| **Instructions** | 1. `card-review-due-v1.avsc` Avro 스키마 작성 (userId, dueCardCount, dueDate)<br>2. Schema Registry에 스키마 등록<br>3. 일일 스케줄러 구현 (@Scheduled, 매일 08:00 KST)<br>4. 복습 대상 사용자 추출 쿼리 (nextReviewDate ≤ today, 카드 수 집계)<br>5. card.review.due Kafka 이벤트 발행 로직 구현<br>6. 배치 발행 최적화 (한 번에 100명씩 처리)<br>7. 통합 테스트: 스케줄러 실행 → 이벤트 발행 확인 |
| **Output Format** | Avro 스키마 + 스케줄러 코드 + 이벤트 발행 테스트 결과 |
| **Constraints** | - 스케줄러 실행 시간: 매일 08:00 KST<br>- 배치 크기: 100명씩 처리<br>- 복습 대상 0건인 사용자는 이벤트 발행하지 않음<br>- 스케줄러 중복 실행 방지 (ShedLock 또는 유사 메커니즘)<br>- 발행 실패 시 재시도 3회 |
| **Duration** | 1.5일 |
| **RULE Reference** | [03-아키텍처](../../wiki/03-아키텍처.md) · [18-기술-스택](../../wiki/18-기술-스택.md) · [14-배포-가이드](../../wiki/14-배포-가이드.md) |
| **Assignee** | @learning-card-owner |
| **Reviewer** | @tech-lead |
| **Status** | TODO |

---

## Step 8: 복습 통계 대시보드

| 필드 | 내용 |
|------|------|
| **Step Name** | 복습 통계 대시보드 |
| **Step Goal** | 사용자가 복습 통계 대시보드(일별/주별 복습 수, 정답률, 스트릭)를 조회할 수 있다. |
| **Done When** | 대시보드 API + 스트릭 계산 + 종합 통계 응답 + 테스트 통과 |
| **Scope** | **In**: 스트릭 계산 로직, 종합 대시보드 API, 캐싱 / **Out**: 프론트엔드 대시보드 UI, 게이미피케이션 연동 |
| **Input** | Step 6 완료된 통계 API, 복습 로그 데이터, PRD_W3 대시보드 요구사항 |
| **Instructions** | 1. 스트릭 계산 로직 구현 (연속 복습 일수)<br>2. 종합 대시보드 API 구현 (GET `/api/v1/stats/dashboard`)<br>3. 응답 데이터: 오늘 복습 수, 주간 복습 수, 정답률, 현재 스트릭, 최장 스트릭<br>4. 스트릭 데이터 저장 테이블 설계 (user_streaks)<br>5. 복습 완료 시 스트릭 자동 업데이트 로직<br>6. 캐싱 적용 (대시보드 데이터 5분 TTL)<br>7. 통합 테스트: 복습 시나리오별 대시보드 데이터 정확도 검증 |
| **Output Format** | 대시보드 API 응답 JSON + 스트릭 계산 로직 + 테스트 결과 |
| **Constraints** | - 스트릭: 연속 복습 일수 (하루라도 빠지면 리셋)<br>- 대시보드 응답 시간 300ms 이내<br>- 캐싱 TTL: 5분<br>- 스트릭은 KST 기준 자정으로 날짜 구분<br>- 복습 0건인 날도 대시보드 표시 |
| **Duration** | 1.5일 |
| **RULE Reference** | [03-아키텍처](../../wiki/03-아키텍처.md) · [18-기술-스택](../../wiki/18-기술-스택.md) |
| **Assignee** | @learning-card-owner |
| **Reviewer** | @tech-lead |
| **Status** | TODO |

---

# W4 (2026-06-01 ~ 2026-06-05, 6/3 지방선거 제외 — 복습 전체 E2E)

## Step 9: 복습 전체 E2E 테스트

| 필드 | 내용 |
|------|------|
| **Step Name** | 복습 전체 E2E 테스트 |
| **Step Goal** | 복습 전체 E2E(카드→복습→SM-2→통계→XP) 시나리오가 통과한다. |
| **Done When** | E2E 테스트 시나리오 전체 통과 + CI 연동 + 테스트 리포트 산출 |
| **Scope** | **In**: E2E 테스트 시나리오 작성, CI 연동, 테스트 리포트 / **Out**: 프로덕션 배포, 모니터링 |
| **Input** | Step 4-8 완료된 전체 복습 기능, CI 파이프라인 설정, E2E 테스트 전략 |
| **Instructions** | 1. E2E 테스트 시나리오 정의 (카드 생성 → 복습 세션 → SM-2 계산 → 통계 조회)<br>2. 카드 CRUD → 복습 → rating → 다음 복습일 E2E 테스트<br>3. Kafka 이벤트 발행/소비 E2E 테스트<br>4. 통계 대시보드 정확도 E2E 테스트<br>5. CI 파이프라인에 E2E 테스트 단계 추가<br>6. 실패 시나리오 테스트 (Kafka 다운, DB 타임아웃)<br>7. 테스트 리포트 자동 생성 설정 |
| **Output Format** | E2E 테스트 코드 + CI 파이프라인 설정 + 테스트 리포트 |
| **Constraints** | - E2E 테스트 실행 시간 5분 이내<br>- CI에서 E2E 실패 시 빌드 FAIL<br>- Testcontainers로 Kafka/PostgreSQL 구동<br>- 테스트 데이터 격리 (테스트 간 독립) |
| **Duration** | 1.5일 |
| **RULE Reference** | [03-아키텍처](../../wiki/03-아키텍처.md) · [09-버전-관리-정책](../../wiki/09-버전-관리-정책.md) · [14-배포-가이드](../../wiki/14-배포-가이드.md) |
| **Assignee** | @learning-card-owner |
| **Reviewer** | @tech-lead |
| **Status** | TODO |

---

## Step 10: P0 버그 수정 및 Kafka 안정화

| 필드 | 내용 |
|------|------|
| **Step Name** | P0 버그 수정 및 Kafka 안정화 |
| **Step Goal** | learning-card의 P0 버그가 모두 수정되고 Kafka 이벤트 발행이 안정화된다. |
| **Done When** | P0 버그 0건 + Kafka 이벤트 발행 성공률 99.9% + 회귀 테스트 통과 |
| **Scope** | **In**: P0 버그 수정, Kafka 발행 안정화, 회귀 테스트 / **Out**: 프로덕션 배포, 신규 기능 추가 |
| **Input** | Step 9 E2E 테스트 결과, P0 버그 목록, Kafka 모니터링 로그 |
| **Instructions** | 1. P0 버그 목록 정리 및 우선순위 배정<br>2. 각 P0 버그 원인 분석 및 수정<br>3. Kafka 발행 실패 원인 분석 (재시도, 타임아웃, 직렬화 에러)<br>4. Kafka Producer 안정화 (acks=all, 재시도 설정 튜닝)<br>5. Dead Letter Queue(DLQ) 설정 (발행 실패 이벤트 보관)<br>6. 회귀 테스트 전체 실행 및 통과 확인<br>7. 수정 사항 코드 리뷰 및 반영 |
| **Output Format** | P0 버그 수정 내역 + Kafka 설정 변경 이력 + 회귀 테스트 결과 |
| **Constraints** | - P0 버그 0건 달성 필수<br>- Kafka 발행 성공률 99.9% 이상<br>- DLQ 설정으로 이벤트 유실 방지<br>- 수정으로 인한 기존 테스트 회귀 금지<br>- 코드 프리즈 전 완료 |
| **Duration** | 1.5일 |
| **RULE Reference** | [03-아키텍처](../../wiki/03-아키텍처.md) · [09-버전-관리-정책](../../wiki/09-버전-관리-정책.md) |
| **Assignee** | @learning-card-owner |
| **Reviewer** | @tech-lead |
| **Status** | TODO |
