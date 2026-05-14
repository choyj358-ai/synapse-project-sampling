# Claude 지침: 조유지 × Learning Card 프로젝트

> **이 문서를 당신의 프로젝트 채팅에 시스템 프롬프트로 복사-붙여넣기 하세요.**
>
> Cowork, Claude Code, 또는 Claude 웹에서 이 지침을 활용하면 Claude가 당신의 프로젝트 맥락을 이해하고 더 효과적으로 도와줄 수 있습니다.

---

## 📌 프로젝트 맥락

- **프로젝트**: Synapse - PKM + SRS + AI 학습 플랫폼
- **당신의 역할**: @learning-card-owner (Learning Card 모듈 담당)
- **팀 구성**: 7명 (팀리드 + 6개 트랙)
- **기간**: 4주 (W1~W4, 2026.05.12 ~ 2026.06.06)
- **배경**: 비전공자 풀스택 학원생

---

## 🎯 당신의 책임 범위

### In Scope (당신이 만들 것)
1. **덱(Deck) CRUD**: 사용자가 학습 카드 모음을 관리
2. **카드(Card) CRUD**: 앞면(질문)/뒷면(답변) 관리
3. **SM-2 알고리즘**: Spaced Repetition 복습 간격 계산
4. **복습 세션(Review Session)**: 오늘 복습할 카드 조회 → 평가 → 다음 복습일 계산
5. **Kafka 이벤트 발행**:
   - `card.reviewed`: XP 적립 트리거 (engagement-owner)
   - `card.review.due`: 복습 리마인더 (platform-owner)
6. **복습 통계 API**: 일별 복습 수, 정답률, 연속 일수(streak)

### Out of Scope (다른 팀)
- AI 카드 자동 생성 (@learning-ai-owner)
- 게이미피케이션 (@engagement-owner)
- 알림 발송 (@platform-owner)

---

## 🛠️ 기술 스택

| 항목 | 사항 |
|------|------|
| **언어** | Java 21 |
| **프레임워크** | Spring Boot 4 |
| **아키텍처** | Modulith (모듈화 설계) |
| **데이터베이스** | PostgreSQL 15+ |
| **메시징** | Kafka (Event-Driven) |
| **빌드** | Gradle Kotlin DSL |
| **테스트** | JUnit 5 + Testcontainers |
| **배포** | Docker + docker-compose |
| **포트** | 8082 |

---

## 📅 주차별 마일스톤

### W1 (05-12 ~ 05-16): 기초 구축
- Step 1: Spring Boot + Modulith 초기 설정
- Step 2: 덱/카드 CRUD API (REST)
- Step 3: SM-2 알고리즘 + 단위테스트

**당신 역할**: 백엔드 API 설계 및 구현

### W2 (05-19 ~ 05-23): 복습 기능
- Step 4: 복습 세션 플로우
- Step 5: card.reviewed Kafka 이벤트
- Step 6: 복습 통계 API

**당신 역할**: 세션 관리, 이벤트 발행

### W3 (05-26 ~ 05-30): 리마인더 & 대시보드
- Step 7: card.review.due 이벤트
- Step 8: 통계 대시보드 (streak, accuracy)

**당신 역할**: 이벤트 안정화, 분석 데이터 제공

### W4 (06-02 ~ 06-06): 안정화
- Step 9: E2E 테스트
- Step 10: 버그 수정

**당신 역할**: 전체 통합 테스트, 프로덕션 준비

---

## 🔗 협업 팀 및 인터페이스

당신이 주고받는 데이터:

| 팀 | 담당자 | 주고받는 것 | 시점 |
|----|--------|-----------|------|
| **Frontend** | 전체 | 복습 세션 API 데이터 | W2 부터 |
| **engagement-owner** | 한승완 | `card.reviewed` 이벤트 → XP | W2 |
| **platform-owner** | 김해준 | `card.review.due` 이벤트 → 알림 | W3 |
| **learning-ai-owner** | 김나경 | AI 생성 카드 수신 | W3 |
| **team-lead** | 김민구 | 코드 리뷰 & 기술 지원 | 항상 |

---

## 💾 저장소 구조 (당신이 작업할 폴더)

```
synapse-learning-svc/
├── src/main/java/
│   └── com/synapse/
│       └── learning/
│           ├── card/              ← 당신의 모듈 1: 덱/카드 CRUD
│           │   ├── controller/
│           │   ├── service/
│           │   ├── repository/
│           │   ├── entity/
│           │   └── dto/
│           ├── srs/                ← 당신의 모듈 2: SM-2 & 복습
│           │   ├── controller/
│           │   ├── service/
│           │   │   └── Sm2Calculator.java  (핵심!)
│           │   ├── repository/
│           │   ├── entity/
│           │   └── dto/
│           └── event/             ← Kafka 이벤트
│               ├── CardReviewedEvent.java
│               └── CardReviewDueEvent.java
├── src/test/java/
│   └── com/synapse/learning/
│       ├── card/
│       │   └── Sm2AlgorithmTest.java  (중요한 테스트!)
│       └── srs/
├── build.gradle.kts                ← 의존성 설정 (팀리드가 제공)
└── docker-compose.yml              ← PostgreSQL + Kafka
```

---

## 🎓 SM-2 알고리즘 (핵심!)

**Spaced Repetition 방식의 복습 간격 계산**

### 입력
- `rating`: 0(Again) / 1(Hard) / 2(Good) / 3(Easy)
- `previousInterval`: 마지막 복습 후 날짜 수
- `previousEF`: 이전 Ease Factor (초기값: 2.5)

### 계산 로직

```
# Ease Factor 계산
q = rating (0~3)
EF_new = EF_old + (0.1 - (5-q) * (0.08 + (5-q) * 0.02))
EF_new = max(1.3, EF_new)  # 최솟값은 1.3

# Interval 계산
if q == 0:  # Again
    interval_new = 1
    repetition = 0
elif q == 1:  # Hard
    interval_new = interval_old  (변경 없음)
    repetition += 1
elif q == 2:  # Good
    interval_new = interval_old * EF_new
    repetition += 1
else:  # q == 3, Easy
    interval_new = interval_old * EF_new * 1.2
    repetition += 1

# 결과
nextReviewDate = today + interval_new days
```

### 예시 흐름

```
카드1: 첫 복습
- rating: Good(2)
- 결과: interval=1*2.5=2.5일, EF=2.36, nextReviewDate=2026-05-14

2.5일 후 복습:
- rating: Easy(3)
- 결과: interval=2.5*2.35*1.2≈7일, EF=2.42, nextReviewDate=2026-05-21

더 이상 복습 안 함:
- 2026-05-21부터 카드 리스트에 나타남
```

---

## 📊 API 명세 (당신이 구현할 것)

### Deck 엔드포인트

```
POST /api/v1/decks
요청:
{
  "title": "JavaScript 고급",
  "description": "클로저와 비동기",
  "isPublic": false
}
응답: { "id": "uuid", "title": "...", ... }

GET /api/v1/decks?page=0&size=20
응답: { "content": [Deck, ...], "totalElements": 5, "hasMore": false }

GET /api/v1/decks/{deckId}
응답: Deck 객체

PUT /api/v1/decks/{deckId}
요청/응답: 덱 정보 업데이트

DELETE /api/v1/decks/{deckId}
응답: HTTP 204
```

### Card 엔드포인트

```
POST /api/v1/decks/{deckId}/cards
요청:
{
  "front": "클로저란 무엇인가?",
  "back": "함수가 자신의 스코프 외부 변수에 접근하는 능력",
  "tags": ["javascript", "scope"]
}
응답: Card 객체

GET /api/v1/decks/{deckId}/cards?page=0&size=20
응답: Card 목록

GET /api/v1/decks/{deckId}/cards/{cardId}
응답: Card 상세

PUT/DELETE: 수정/삭제
```

### Review Session 엔드포인트

```
POST /api/v1/review-sessions
요청: { "deckId": "uuid" }
응답: { "id": "session-uuid", "cardCount": 5, "startedAt": "..." }

GET /api/v1/review-sessions/{sessionId}/cards/today
응답: 오늘 복습할 카드 목록 (최대 50개)

POST /api/v1/review-sessions/{sessionId}/cards/{cardId}/rate
요청:
{
  "rating": 2,  // 0(Again) ~ 3(Easy)
  "reviewedAt": "2026-05-14T14:30:00Z"
}
응답:
{
  "cardId": "...",
  "rating": 2,
  "nextReviewDate": "2026-05-16",  // SM-2로 계산됨
  "easeFactor": 2.42,
  "interval": 2.5
}
```

### Statistics 엔드포인트

```
GET /api/v1/stats/review-summary?period=WEEK
응답:
{
  "totalReviews": 42,
  "correctCount": 35,
  "accuracy": 0.833,
  "dailyBreakdown": [
    { "date": "2026-05-14", "count": 6, "correct": 5 },
    ...
  ]
}

GET /api/v1/stats/streak
응답: { "currentStreak": 5, "longestStreak": 12 }
```

---

## 🧪 테스트 전략

### Unit Test (단위테스트)
**초점**: SM-2 알고리즘 검증

```java
@Test
void testSm2GoodRating() {
  // Given
  Sm2Calculator calc = new Sm2Calculator();
  
  // When
  Sm2Result result = calc.calculateNext(
    rating=2,  // Good
    previousInterval=1.0,
    previousEF=2.5
  );
  
  // Then
  assertEquals(2.5, result.getInterval());
  assertEquals(2.36, result.getEaseFactor());
}
```

- **작성 시점**: Step 3 (SM-2 알고리즘)
- **커버리지**: 4 rating × 3 경계값 = 12개 케이스
- **도구**: JUnit 5

### Integration Test (통합테스트)
**초점**: API 엔드포인트 + DB 트랜잭션

```java
@Test
@Sql({"/test-setup.sql"})
void testReviewSessionFlow() {
  // POST 복습 시작
  // GET 오늘 카드
  // POST 카드 평가
  // 검증: SM-2 계산, DB 업데이트
}
```

- **작성 시점**: Step 2, 4 이후
- **도구**: Testcontainers (실제 PostgreSQL 띄움)

### E2E Test (엔드투엔드 테스트)
**초점**: 전체 사용자 플로우

```
1. 덱 생성
2. 카드 5장 추가
3. 복습 세션 시작
4. 5장 모두 평가
5. 통계 확인
6. Kafka 이벤트 수신 확인
```

- **작성 시점**: W4 Step 9
- **도구**: RestAssured + Embedded Kafka

---

## ✅ 각 Step의 Definition of Done

### Step 1: 초기 설정
- [x] Spring Boot 프로젝트 생성 (Java 21, Gradle)
- [x] Modulith 의존성 추가 + `card`, `srs` 모듈 생성
- [x] `ApplicationModules.verify()` 테스트 통과
- [x] Health endpoint (`/actuator/health`) 응답 정상
- [x] docker-compose.yml (PostgreSQL 포함) 작동
- [x] `.gitignore`, `Dockerfile` 설정 완료

### Step 2: 덱/카드 CRUD
- [x] Deck, Card JPA Entity 작성
- [x] Flyway 마이그레이션 스크립트 작성
- [x] DeckRepository, CardRepository 구현
- [x] DeckService, CardService 비즈니스 로직
- [x] REST Controller 6개 엔드포인트 모두 구현
- [x] 페이지네이션 (Pageable) 적용
- [x] 통합테스트 8개 이상 (CRUD 각각)
- [x] 입력값 검증 (Bean Validation) 추가

### Step 3: SM-2 알고리즘
- [x] `Sm2Calculator` 서비스 클래스 구현
- [x] `rating` enum 정의 (Again=0, Hard=1, Good=2, Easy=3)
- [x] `Sm2Result` DTO 정의 (nextInterval, easeFactor)
- [x] 단위테스트 12개 (4 rating × 3 경계값)
- [x] Card에 `easeFactor`, `interval`, `repetition` 필드 추가
- [x] `review_logs` 테이블 마이그레이션
- [x] POST `/api/v1/cards/{cardId}/review` 엔드포인트 구현

### Step 4: 복습 세션
- [x] ReviewSession, CardRating Entity
- [x] `review_sessions` 테이블 마이그레이션
- [x] POST `/api/v1/review-sessions` (세션 시작)
- [x] GET `/api/v1/review-sessions/{id}/cards/today`
- [x] POST `.../cards/{cardId}/rate` (평가 + SM-2 계산)
- [x] 통합테스트: 세션 시작 → 카드 조회 → 평가 → 검증

### Step 5: Kafka 이벤트
- [x] `CardReviewedEvent` 클래스 (AVRO 스키마)
- [x] `CardReviewDueEvent` 클래스
- [x] Spring Cloud Stream으로 Kafka 발행
- [x] 이벤트 발행 로직을 Step 4의 평가 API에 통합
- [x] Embedded Kafka로 테스트

### Step 6: 통계 API
- [x] `ReviewStatistics` Entity (일별 집계 데이터)
- [x] GET `/api/v1/stats/review-summary` (일별/주별)
- [x] 정답률(`accuracy`) 계산
- [x] 통합테스트

### Step 7: card.review.due 이벤트
- [x] Scheduler (예: 매일 자정에 `card.review.due` 발행)
- [x] 또는 카드 평가 후 다음 복습일이 오늘이면 즉시 발행
- [x] 이벤트 안정화 (재전송 로직)

### Step 8: 대시보드 통계
- [x] GET `/api/v1/stats/streak` (연속 복습 일수)
- [x] 월별/분기별 통계 추가
- [x] 테스트

### Step 9: E2E 테스트
- [x] 전체 플로우 테스트 (덱 생성 → 카드 추가 → 복습 → 통계)
- [x] 테스트 커버리지 70% 이상

### Step 10: 버그 수정 & 배포
- [x] W1~W3 중 발견된 버그 수정
- [x] Kafka 메시지 순서 보장
- [x] 동시성 문제 해결 (테스트)
- [x] 배포 준비

---

## 🚀 개발 시 Claude 활용 팁

### 1. 코드 리뷰 요청
**당신**: "이 SM-2 계산 로직이 맞나요?"
```
(코드 붙여넣기)
```
**Claude**: 수식 검증 + 테스트 케이스 제안

### 2. 에러 디버깅
**당신**: "Kafka 이벤트가 발행되지 않아요"
```
(에러 로그 붙여넣기)
```
**Claude**: 원인 분석 + 해결책 제시

### 3. 구현 가이드
**당신**: "복습 세션 API를 어떻게 구조화할까요?"
**Claude**: 
- Entity 설계
- Service 로직 (pseudo code)
- Controller 엔드포인트
- 테스트 케이스

### 4. SQL 쿼리
**당신**: "오늘 복습할 카드를 가져오는 쿼리를 짜줄 수 있나요?"
**Claude**: JPQL 또는 native SQL + 인덱스 팁

### 5. 테스트 작성
**당신**: "SM-2 unit test를 좀 도와줘"
**Claude**: JUnit 5 + Parameterized Test 템플릿

---

## 📝 당신이 Claude와 대화할 때의 맥락 설정

**첫 대화에서 이렇게 말하세요:**

> "나는 조유지야. Synapse 팀프로젝트에서 Learning Card 모듈(덱, 카드, SM-2, 복습 세션)을 구현 중이야.
> 
> 기술 스택: Java 21, Spring Boot 4, Modulith, PostgreSQL, Kafka
> 
> 지금 W1 Step 2(덱/카드 CRUD)를 진행 중이야. 
> 
> [구체적인 질문 또는 코드 붙여넣기]"

**그러면 Claude가 다음을 자동으로 이해합니다:**
- ✅ 당신의 역할
- ✅ 프로젝트 스코프
- ✅ 사용 기술
- ✅ 현재 진행 단계

---

## 🎯 일주일 개발 리듬

### 월요일 (Sprint 시작)
- [ ] 팀리드와 주간 킥오프 (Slack)
- [ ] 이번 주 Step의 "Done When" 재확인
- [ ] 의존성 확인 (다른 팀 진행상황)

### 화~목 (개발)
- [ ] 매일 코드 작성 + 테스트
- [ ] 막히는 부분 = Claude에 물어보기
- [ ] 팀 Slack에서 최신 정보 확인

### 금요일 (검수 & 종료)
- [ ] 팀리드에게 코드 리뷰 요청
- [ ] 주간 완료 현황 보고
- [ ] 다음 주 준비

---

## 💡 비전공자라서 힘들 수 있는 부분 (Claude와 함께!)

| 어려움 | Claude와 하는 방식 |
|--------|------------------|
| **Spring Boot 구조 이해** | "Spring Boot 프로젝트 구조를 그려줄 수 있어?" |
| **JPA/Hibernate** | "Card와 Deck의 @OneToMany 관계를 어떻게 매핑하지?" |
| **SQL 쿼리** | "oday 복습할 카드를 가져오는 쿼리를 짜줄 수 있나?" |
| **JSON 포맷** | "이 API 응답을 어떻게 구조화하면 좋을까?" |
| **테스트 작성** | "JUnit 5로 이 로직을 테스트하는 방식을 보여줄 수 있나?" |
| **Kafka 개념** | "Kafka Topic/Producer/Consumer가 뭔지 쉽게 설명해줄 수 있나?" |

---

## 📚 참고 자료 링크

- [Spring Boot Official](https://spring.io/projects/spring-boot)
- [Modulith Docs](https://github.com/odrotbohm/modulith)
- [PostgreSQL Docs](https://www.postgresql.org/docs/)
- [Kafka Tutorial](https://kafka.apache.org/documentation/)
- [SM-2 Algorithm](https://supermemory.com/en/spaced-repetition)
- [JUnit 5](https://junit.org/junit5/)

---

## ✨ 최종 조언

1. **천천히 진행하세요**: 각 Step은 명확한 요구사항이 있습니다. 서두르지 말 것.

2. **테스트는 당신의 친구**: 코드를 짜면서 테스트도 함께 작성하면, 나중에 버그가 줄어요.

3. **Claude 활용하세요**:
   - 막힐 때마다 물어보세요 (수정 사항, 설명, 예시 등)
   - 코드를 보여주고 피드백 받으세요
   - 구현 전에 설계 상담도 가능해요

4. **팀에 물어보세요**:
   - 아키텍처 의사결정은 팀리드(김민구)와
   - 프론트엔드 연동은 frontend-owner와
   - Kafka 통합은 platform-owner와

5. **문서를 자주 읽으세요**: 
   - 이 파일
   - 조유지_프로젝트_종합_가이드.md
   - 팀의 SCOPE, TASK, WORKFLOW 문서들

**당신은 할 수 있어요! 화이팅! 🚀**

---

## 🔗 빠른 참조

### 매 주마다 확인할 문서
- W1: `TASK_learning-card.md` (Step 1-3)
- W2: `WORKFLOW_learning-card_W2.md`
- W3: `WORKFLOW_learning-card_W3.md`
- W4: `WORKFLOW_learning-card_W4.md`

### GitHub 주소
https://github.com/team-project-final/synapse-learning-svc (learning-card branch)

### 팀 슬랙
#learning-card (질문, 공지, 완료 보고)

### 당신의 이메일
choyj358@gmail.com

---

**작성일**: 2026-05-14
**버전**: v1.0
**담당자**: 조유지
