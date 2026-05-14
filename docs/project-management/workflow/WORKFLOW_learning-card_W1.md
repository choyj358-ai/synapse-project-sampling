# WORKFLOW: @learning-card-owner — Week 1

> **Task 문서**: [TASK_learning-card.md](../task/TASK_learning-card.md)  
> **기간**: 2026-05-12 ~ 2026-05-16  
> **기능개발 Workflow**: [README §7](../README.md)

---

## Step 1: 프로젝트 초기 설정

### 1.1 TASK 시작
- [ ] Step Goal / Done When / Scope / Input 확인
- [ ] PRD_W1 해당 요구사항 확인 (프로젝트 골격)
- [ ] Duration 산정 확인 (0.5일)

### 1.2 요구사항 분석
- [ ] Spring Boot 4 + Modulith 프로젝트 구조 분석
- [ ] card/srs 2개 모듈 역할 정의
- [ ] 팀 공통 Gradle 설정 확인
- [ ] Instructions 초안 → TASK 문서 반영

### 1.3 Security 1차 검토
- [ ] 인증 필요 여부: No (골격만 생성)
- [ ] 권한 종류: 없음
- [ ] 공개 API 여부: No (Health endpoint만)
- [ ] 결과 → TASK Constraints 반영

### 1.4 ERD 설계
- [ ] 골격 단계 — ERD 해당 없음
- [ ] 모듈별 패키지 구조도 작성
- [ ] Duration(final) 갱신

### 1.5 Security 2차 검토
- [ ] 민감 정보 암호화: 비해당 (골격 단계)
- [ ] Soft Delete 정책: 비해당
- [ ] 행 단위 접근 제어: 불필요
- [ ] 결과 → TASK Constraints 반영

### 1.6 DTO / Entity 설계 (API First)
- [ ] 골격 단계 — 빈 Controller/Service 클래스만 생성
- [ ] 각 모듈 package-info.java + @ApplicationModule 설정
- [ ] Output Format → TASK 반영

### 1.7 Repository 구현
- [ ] 골격 단계 — Repository 해당 없음
- [ ] ApplicationModulesTest 구조 검증 테스트 작성

### 1.8 Service + Test
- [ ] 빈 Service 클래스 생성 (card, srs 모듈)
- [ ] ApplicationModulesTest 통과 확인
- [ ] `./gradlew build` 성공 확인
- [ ] Health endpoint (`/actuator/health`) 확인

### 1.9 Controller + Test
- [ ] 빈 Controller 클래스 생성 (card, srs 모듈)
- [ ] Dockerfile 작성 (multi-stage build)
- [ ] Docker 이미지 빌드 성공 확인

### 1.10 View + Test (해당 시)
- [ ] Flutter 화면 연동: 해당 없음
- [ ] docker compose에서 learning-card-svc 실행 확인
- [ ] RULE Reference → TASK 반영

**Step 1 Status**: [ ] Not Started / [ ] In Progress / [ ] Done

---

## Step 2: 덱/카드 CRUD API

### 1.1 TASK 시작
- [ ] Step Goal / Done When / Scope / Input 확인
- [ ] PRD_W1 해당 요구사항 확인 (FR-LC-xxx 덱/카드 CRUD)
- [ ] Duration 산정 확인 (2일)

### 1.2 요구사항 분석
- [ ] Deck/Card CRUD API 엔드포인트 정의
- [ ] 1:N 관계 (Deck → Card) RESTful URL 규칙 확인
- [ ] 페이지네이션 요건 (기본 20건, 최대 100건)
- [ ] Instructions 초안 → TASK 문서 반영

### 1.3 Security 1차 검토
- [ ] 인증 필요 여부: Yes (JWT 인증 필요, userId는 JWT에서 추출)
- [ ] 권한 종류: 로그인 사용자 (본인 덱/카드만)
- [ ] 공개 API 여부: No
- [ ] 결과 → TASK Constraints 반영

### 1.4 ERD 설계
- [ ] decks 테이블 설계 (id, userId, title, description, createdAt, updatedAt, deletedAt)
- [ ] cards 테이블 설계 (id, deckId, front, back, createdAt, updatedAt, deletedAt)
- [ ] 인덱스 설계 (decks.userId, cards.deckId)
- [ ] 관계 정의 (cards.deckId → decks.id FK)
- [ ] Duration(final) 갱신

### 1.5 Security 2차 검토
- [ ] 민감 정보 암호화: 비해당
- [ ] Soft Delete 정책: 논리삭제 (deletedAt 컬럼)
- [ ] 행 단위 접근 제어: 필요 (userId 기반 덱 소유 확인)
- [ ] 결과 → TASK Constraints 반영

### 1.6 DTO / Entity 설계 (API First)
- [ ] DeckCreateRequest 정의 (title, description)
- [ ] DeckResponse 정의 (id, title, description, cardCount, createdAt)
- [ ] CardCreateRequest 정의 (front, back)
- [ ] CardResponse 정의 (id, front, back, createdAt)
- [ ] Deck Entity 작성
- [ ] Card Entity 작성
- [ ] MapStruct 매퍼 작성
- [ ] Output Format → TASK 반영

### 1.7 Repository 구현
- [ ] DeckRepository 인터페이스 작성
- [ ] CardRepository 인터페이스 작성
- [ ] findByUserIdAndDeletedAtIsNull 커스텀 쿼리
- [ ] Flyway 마이그레이션 스크립트 작성

### 1.8 Service + Test
- [ ] DeckService CRUD 구현 (create, findAll, findById, update, delete)
- [ ] CardService CRUD 구현 (create, findByDeckId, update, delete)
- [ ] 소유자 검증 로직 구현
- [ ] Bean Validation 적용
- [ ] 단위 테스트 작성 (Mockito)
- [ ] 테스트 통과 확인

### 1.9 Controller + Test
- [ ] DeckController REST API 구현 (POST/GET/PUT/DELETE)
- [ ] CardController REST API 구현 (`/api/v1/decks/{deckId}/cards`)
- [ ] 슬라이스 테스트 (@WebMvcTest)
- [ ] 401/403 응답 테스트
- [ ] 통합 테스트 (각 엔드포인트별)
- [ ] 테스트 통과 확인

### 1.10 View + Test (해당 시)
- [ ] Flutter 화면 연동: 해당 없음 (프론트 별도)
- [ ] Swagger API 문서 확인
- [ ] RULE Reference → TASK 반영

**Step 2 Status**: [ ] Not Started / [ ] In Progress / [ ] Done

---

## Step 3: SM-2 알고리즘 구현

### 1.1 TASK 시작
- [ ] Step Goal / Done When / Scope / Input 확인
- [ ] PRD_W1 해당 요구사항 확인 (FR-LC-xxx SM-2 복습)
- [ ] Duration 산정 확인 (1.5일)

### 1.2 요구사항 분석
- [ ] SM-2 알고리즘 논문/명세 분석
- [ ] rating enum 정의 (Again=0, Hard=1, Good=2, Easy=3)
- [ ] interval/EF 계산 규칙 정리
- [ ] Instructions 초안 → TASK 문서 반영

### 1.3 Security 1차 검토
- [ ] 인증 필요 여부: Yes (JWT 인증 필요)
- [ ] 권한 종류: 로그인 사용자 (본인 카드만 복습)
- [ ] 공개 API 여부: No
- [ ] 결과 → TASK Constraints 반영

### 1.4 ERD 설계
- [ ] review_logs 테이블 설계 (id, cardId, userId, rating, interval, easeFactor, reviewedAt)
- [ ] 인덱스 설계 (cardId+userId, reviewedAt DESC)
- [ ] 관계 정의 (review_logs.cardId → cards.id FK)
- [ ] Duration(final) 갱신

### 1.5 Security 2차 검토
- [ ] 민감 정보 암호화: 비해당
- [ ] Soft Delete 정책: 물리삭제 (로그 누적 보관)
- [ ] 행 단위 접근 제어: 필요 (본인 카드 복습 기록만)
- [ ] 결과 → TASK Constraints 반영

### 1.6 DTO / Entity 설계 (API First)
- [ ] ReviewRequest 정의 (rating)
- [ ] ReviewResponse 정의 (nextReviewDate, interval, easeFactor)
- [ ] ReviewLog Entity 작성
- [ ] Rating Enum 작성 (AGAIN, HARD, GOOD, EASY)
- [ ] MapStruct 매퍼 작성
- [ ] Output Format → TASK 반영

### 1.7 Repository 구현
- [ ] ReviewLogRepository 인터페이스 작성
- [ ] findTopByCardIdOrderByReviewedAtDesc 커스텀 쿼리
- [ ] Flyway 마이그레이션 스크립트 작성

### 1.8 Service + Test
- [ ] Sm2Calculator 도메인 서비스 구현
- [ ] interval 계산 로직 (Again→1, Hard→유지, Good→interval*EF, Easy→interval*EF*2)
- [ ] easeFactor 업데이트 로직 (최소 1.3 보장)
- [ ] 단위 테스트 작성 (4개 rating x 초기/중간/고EF 경계값)
- [ ] 부동소수점 반올림 처리 확인
- [ ] 테스트 통과 확인

### 1.9 Controller + Test
- [ ] POST /api/v1/cards/{cardId}/review 엔드포인트 구현
- [ ] 슬라이스 테스트 (@WebMvcTest)
- [ ] 401/403 응답 테스트
- [ ] 통합 테스트 (복습 → interval 변경 확인)
- [ ] 테스트 통과 확인

### 1.10 View + Test (해당 시)
- [ ] Flutter 화면 연동: 해당 없음 (프론트 별도)
- [ ] Swagger API 문서 확인
- [ ] RULE Reference → TASK 반영

**Step 3 Status**: [ ] Not Started / [ ] In Progress / [ ] Done
