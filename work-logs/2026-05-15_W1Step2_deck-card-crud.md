# W1 Step 2 — 덱/카드 CRUD API

**날짜**: 2026-05-15  
**담당**: @learning-card-owner  
**소요 시간**: 2일 (샘플링으로 단일 세션)

---

## 생성/수정 파일 목록

| 파일 | 작업 |
|------|------|
| `build.gradle.kts` | 수정 — JPA, H2, Flyway, Lombok 의존성 추가 |
| `application.properties` | 수정 — H2 datasource, Flyway, pageable 설정 |
| `src/test/resources/application.properties` | 신규 — 테스트 전용 (Flyway 비활성화, ddl-auto=create-drop) |
| `db/migration/V1__create_decks.sql` | 신규 |
| `db/migration/V2__create_cards.sql` | 신규 |
| `BusinessException.java` | 신규 — base exception |
| `GlobalExceptionHandler.java` | 신규 — ProblemDetail 기반 예외 처리 |
| `card/exception/DeckNotFoundException.java` | 신규 |
| `card/exception/CardNotFoundException.java` | 신규 |
| `card/Deck.java` | 신규 — Entity (soft delete) |
| `card/Card.java` | 신규 — Entity (soft delete) |
| `card/DeckRepository.java` | 신규 |
| `card/CardRepository.java` | 신규 |
| `card/dto/Deck(Create/Update/Response).java` | 신규 — Java record |
| `card/dto/Card(Create/Update/Response).java` | 신규 — Java record |
| `card/DeckService.java` | 신규 |
| `card/CardService.java` | 수정 (stub → 구현) |
| `card/DeckController.java` | 신규 |
| `card/CardController.java` | 수정 (stub → 구현) |
| `card/DeckRepositoryTest.java` | 신규 — @SpringBootTest @Transactional |
| `card/CardRepositoryTest.java` | 신규 — @SpringBootTest @Transactional |
| `card/DeckControllerTest.java` | 신규 — @SpringBootTest + MockMvcBuilders |
| `card/CardControllerTest.java` | 신규 — @SpringBootTest + MockMvcBuilders |

---

## 빌드 결과

```
BUILD SUCCESSFUL in 16s
21 tests completed, 0 failed
```

---

## API 엔드포인트

| Method | URL | 응답 |
|--------|-----|------|
| POST | `/api/v1/decks` | 201 |
| GET | `/api/v1/decks` | 200 (Page) |
| GET | `/api/v1/decks/{deckId}` | 200 |
| PUT | `/api/v1/decks/{deckId}` | 200 |
| DELETE | `/api/v1/decks/{deckId}` | 204 (soft delete) |
| POST | `/api/v1/decks/{deckId}/cards` | 201 |
| GET | `/api/v1/decks/{deckId}/cards` | 200 (Page) |
| GET | `/api/v1/decks/{deckId}/cards/{cardId}` | 200 |
| PUT | `/api/v1/decks/{deckId}/cards/{cardId}` | 200 |
| DELETE | `/api/v1/decks/{deckId}/cards/{cardId}` | 204 (soft delete) |

---

## 학습 내용

- Spring Boot 4에서 `@WebMvcTest`, `@DataJpaTest`가 완전 제거됨 → `@SpringBootTest` + `MockMvcBuilders.webAppContextSetup(wac).build()` 방식으로 대응
- Spring Boot 4에서 테스트 시 `@MockitoBean`(Spring Framework 7)을 사용 (`@MockBean` 대신)
- H2 테스트 환경에서 Flyway 대신 `ddl-auto=create-drop` 사용 (테스트 독립성 확보)
- `X-User-Id` 헤더 기반 stub 방식으로 JWT 인증 우회 (추후 JWT 필터로 교체 예정)
- Soft delete: `deletedAt` 컬럼으로 논리 삭제 구현

---

## 특이사항

- Spring Boot 4.0.0에서 test slice 지원 완전 제거 — `spring-boot-test-autoconfigure` JAR에서 web/jpa 패키지 없음
- `@MockitoBean`과 `@SpringBootTest` 함께 쓸 때 Spring 컨텍스트에서 `ObjectMapper` bean이 주입 안 되는 현상 → 테스트 클래스에서 `new ObjectMapper()` 직접 생성으로 해결
- PostgreSQL 전환 시: application.properties에서 H2 → PostgreSQL datasource로 변경, `spring.flyway.enabled=true`, `ddl-auto=none` 복원 필요
