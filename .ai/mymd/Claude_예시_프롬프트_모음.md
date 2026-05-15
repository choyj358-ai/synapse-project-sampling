# Claude와 함께하는 Learning Card 개발: 예시 프롬프트 모음

> **이 파일을 북마크하고**, 막힐 때마다 비슷한 상황의 프롬프트를 참고하세요.
>
> 각 프롬프트는 Copy-Paste 가능하도록 만들었습니다. 필요에 맞게 수정해서 사용하세요.

---

## 📋 목차
1. [초기 설정 & 구조 설계](#초기-설정--구조-설계)
2. [CRUD API 개발](#crud-api-개발)
3. [SM-2 알고리즘](#sm2-알고리즘)
4. [복습 세션](#복습-세션)
5. [Kafka 이벤트](#kafka-이벤트)
6. [테스트 작성](#테스트-작성)
7. [버그 디버깅](#버그-디버깅)

---

# 초기 설정 & 구조 설계

## 예시 1: Spring Boot 프로젝트 생성 방법

**당신의 질문:**
```
나는 조유지고 Synapse 팀프로젝트 Learning Card Owner야.
Spring Boot 4 + Java 21로 새 프로젝트를 만들어야 하는데, 
Spring Initializr에서 어떤 의존성을 선택해야 할까?

필요한 것:
- Modulith (모듈화)
- JPA / PostgreSQL
- Kafka
- 액추에이터 (Health endpoint)

선택지를 가이드해줄 수 있나?
```

**Claude의 답변 예시:**
```
네! Spring Initializr에서 다음 순서로 진행하세요:

1. 기본 정보
   - Project: Gradle - Kotlin DSL
   - Language: Java
   - Spring Boot: 4.0.0
   - Java: 21

2. Dependencies 검색 후 추가:
   - "Spring Data JPA" (데이터베이스)
   - "PostgreSQL Driver"
   - "Spring Cloud Stream" (Kafka)
   - "Modulith" (모듈화)
   - "Spring Boot Actuator" (Health)
   - "Spring Web" (REST API)

3. 프로젝트 생성 후 로컬에서:
   ./gradlew build
   ./gradlew bootRun
```

---

## 예시 2: Modulith 모듈 구조 설계

**당신의 질문:**
```
Modulith에서 card와 srs 모듈을 어떻게 분리할까?

생각하는 구조:
- card: 덱/카드 CRUD, 카드 저장소
- srs: SM-2 알고리즘, 복습 세션

이 분리가 맞나? 모듈간 의존성은 어떻게 처리할까?
```

**Claude의 답변 예시:**
```
좋은 구조야! 세부 정리:

card 모듈:
├── controller/
│   └── DeckCardController.java
├── service/
│   ├── DeckService.java
│   └── CardService.java
├── repository/
│   ├── DeckRepository.java
│   └── CardRepository.java
├── entity/
│   ├── Deck.java
│   └── Card.java
└── dto/
    ├── CreateCardRequest.java
    └── CardResponse.java

srs 모듈:
├── controller/
│   ├── ReviewSessionController.java
│   └── StatisticsController.java
├── service/
│   ├── ReviewSessionService.java
│   ├── Sm2Calculator.java  ← 핵심!
│   └── StatisticsService.java
├── repository/
│   ├── ReviewSessionRepository.java
│   └── ReviewLogRepository.java
└── entity/
    ├── ReviewSession.java
    └── ReviewLog.java

모듈간 의존성:
- srs → card: card 엔티티에 접근 (읽기만)
- card → srs: 접근 금지 (순환 참조 방지)

card가 Kafka 이벤트 발행할 때:
- ApplicationEventPublisher를 주입 받아서 발행
- srs는 이 이벤트를 수신 (옵셔널)
```

---

# CRUD API 개발

## 예시 3: Deck CRUD API 한 번에 구현

**당신의 질문:**
```
Deck CRUD API를 작성하는데, 
Controller → Service → Repository 계층을 
어떻게 구조화할까?

요구사항:
- POST /decks (생성)
- GET /decks (목록, 페이지네이션)
- GET /decks/{id} (상세)
- PUT /decks/{id} (수정)
- DELETE /decks/{id} (삭제)
```

**Claude의 답변 예시:**
```
스켈레톤 코드:

=== Entity ===
@Entity @Table(name = "decks")
public class Deck {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    private UUID userId;
    private String title;
    private String description;
    private boolean isPublic;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;  // soft delete
}

=== Repository ===
@Repository
public interface DeckRepository extends JpaRepository<Deck, UUID> {
    Page<Deck> findByUserIdAndDeletedAtIsNull(
        UUID userId, 
        Pageable pageable
    );
}

=== Service ===
@Service @RequiredArgsConstructor
public class DeckService {
    private final DeckRepository repo;
    private final JwtUtil jwtUtil;
    
    public DeckResponse create(CreateDeckRequest req) {
        Deck deck = Deck.builder()
            .userId(getCurrentUserId())  // JWT에서 추출
            .title(req.getTitle())
            .description(req.getDescription())
            .isPublic(req.isPublic())
            .createdAt(LocalDateTime.now())
            .build();
        return toResponse(repo.save(deck));
    }
    
    public Page<DeckResponse> getMyDecks(Pageable page) {
        return repo.findByUserIdAndDeletedAtIsNull(
            getCurrentUserId(), page
        ).map(this::toResponse);
    }
}

=== Controller ===
@RestController @RequestMapping("/api/v1/decks")
public class DeckController {
    private final DeckService service;
    
    @PostMapping
    public ResponseEntity<DeckResponse> create(
        @RequestBody @Valid CreateDeckRequest req
    ) {
        return ResponseEntity.status(201)
            .body(service.create(req));
    }
    
    @GetMapping
    public ResponseEntity<Page<DeckResponse>> getMyDecks(
        @PageableDefault(size = 20) Pageable page
    ) {
        return ResponseEntity.ok(service.getMyDecks(page));
    }
}

=== Test ===
@SpringBootTest
class DeckControllerTest {
    @Autowired private WebTestClient client;
    @Autowired private DeckRepository repo;
    
    @Test
    void testCreateDeck() {
        CreateDeckRequest req = new CreateDeckRequest(
            "Java 기초", "변수와 타입", false
        );
        
        client.post().uri("/api/v1/decks")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(req)
            .exchange()
            .expectStatus().isCreated()
            .expectBody()
            .jsonPath("$.id").isNotEmpty()
            .jsonPath("$.title").isEqualTo("Java 기초");
    }
}
```

---

## 예시 4: Card CRUD (Deck 하위 리소스)

**당신의 질문:**
```
Card는 Deck의 하위 리소스야.
URL 구조:
- POST /decks/{deckId}/cards
- GET /decks/{deckId}/cards
- DELETE /decks/{deckId}/cards/{cardId}

Card 엔티티와 Controller를 어떻게 설계할까?
```

**Claude의 답변 예시:**
```
=== Entity ===
@Entity @Table(name = "cards")
public class Card {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne @JoinColumn(name = "deck_id")
    private Deck deck;
    
    private String front;  // 질문
    private String back;   // 답변
    private String tags;   // 쉼표로 구분
    
    // SM-2 필드 (Step 3에서 추가)
    private double easeFactor = 2.5;
    private double interval = 1.0;
    private int repetition = 0;
    private LocalDate nextReviewDate;
    
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;  // soft delete
}

=== Controller ===
@RestController
@RequestMapping("/api/v1/decks")
public class CardController {
    private final CardService service;
    
    @PostMapping("/{deckId}/cards")
    public ResponseEntity<CardResponse> create(
        @PathVariable UUID deckId,
        @RequestBody @Valid CreateCardRequest req
    ) {
        return ResponseEntity.status(201)
            .body(service.create(deckId, req));
    }
    
    @GetMapping("/{deckId}/cards")
    public ResponseEntity<Page<CardResponse>> list(
        @PathVariable UUID deckId,
        @PageableDefault(size = 20) Pageable page
    ) {
        return ResponseEntity.ok(service.listByDeck(deckId, page));
    }
    
    @GetMapping("/{deckId}/cards/{cardId}")
    public ResponseEntity<CardResponse> getOne(
        @PathVariable UUID deckId,
        @PathVariable UUID cardId
    ) {
        return ResponseEntity.ok(service.getCard(deckId, cardId));
    }
    
    @PutMapping("/{deckId}/cards/{cardId}")
    public ResponseEntity<CardResponse> update(
        @PathVariable UUID deckId,
        @PathVariable UUID cardId,
        @RequestBody @Valid UpdateCardRequest req
    ) {
        return ResponseEntity.ok(service.update(deckId, cardId, req));
    }
    
    @DeleteMapping("/{deckId}/cards/{cardId}")
    public ResponseEntity<Void> delete(
        @PathVariable UUID deckId,
        @PathVariable UUID cardId
    ) {
        service.delete(deckId, cardId);
        return ResponseEntity.noContent().build();
    }
}

=== Service ===
@Service @RequiredArgsConstructor
public class CardService {
    private final CardRepository repo;
    private final DeckRepository deckRepo;
    
    public CardResponse create(UUID deckId, CreateCardRequest req) {
        Deck deck = deckRepo.findById(deckId)
            .orElseThrow(() -> new NotFoundException("Deck not found"));
        
        Card card = Card.builder()
            .deck(deck)
            .front(req.getFront())
            .back(req.getBack())
            .tags(String.join(",", req.getTags()))
            .nextReviewDate(LocalDate.now())
            .createdAt(LocalDateTime.now())
            .build();
        
        return toResponse(repo.save(card));
    }
}
```

---

# SM-2 알고리즘

## 예시 5: SM-2 알고리즘 구현 (핵심!)

**당신의 질문:**
```
SM-2 알고리즘을 구현하려면 어디서부터 시작할까?

요구사항:
- rating (0:Again, 1:Hard, 2:Good, 3:Easy) 입력
- interval, easeFactor 계산
- nextReviewDate 반환

테스트도 보여줄 수 있나?
```

**Claude의 답변 예시:**
```
=== Sm2Calculator (서비스 클래스) ===
@Service
public class Sm2Calculator {
    
    /**
     * SM-2 알고리즘으로 다음 복습 정보 계산
     */
    public Sm2Result calculate(Sm2Input input) {
        double newEF = calculateEaseFactor(
            input.getRating(), 
            input.getOldEaseFactor()
        );
        
        double newInterval = calculateInterval(
            input.getRating(),
            input.getOldInterval(),
            newEF,
            input.getRepetition()
        );
        
        LocalDate nextReviewDate = LocalDate.now()
            .plusDays((long) newInterval);
        
        return Sm2Result.builder()
            .easeFactor(newEF)
            .interval(newInterval)
            .nextReviewDate(nextReviewDate)
            .build();
    }
    
    private double calculateEaseFactor(int rating, double oldEF) {
        // EF_new = EF_old + (0.1 - (5-q) * (0.08 + (5-q) * 0.02))
        double q = rating;  // 0~3
        double delta = 0.1 - (5 - q) * (0.08 + (5 - q) * 0.02);
        double newEF = oldEF + delta;
        
        // 최솟값 1.3 보장
        return Math.max(1.3, newEF);
    }
    
    private double calculateInterval(int rating, double oldInterval, 
                                    double newEF, int repetition) {
        return switch(rating) {
            case 0 -> 1.0;           // Again
            case 1 -> oldInterval;   // Hard (변경 없음)
            case 2 -> oldInterval * newEF;           // Good
            case 3 -> oldInterval * newEF * 1.2;    // Easy
            default -> throw new IllegalArgumentException(
                "rating must be 0-3"
            );
        };
    }
}

=== Input/Output DTO ===
@Data @Builder
public class Sm2Input {
    private int rating;           // 0~3
    private double oldInterval;   // 이전 간격 (일)
    private double oldEaseFactor; // 이전 EF
    private int repetition;       // 반복 횟수
}

@Data @Builder
public class Sm2Result {
    private double interval;
    private double easeFactor;
    private LocalDate nextReviewDate;
}

=== Unit Test ===
@Test
class Sm2CalculatorTest {
    
    private Sm2Calculator calc;
    
    @BeforeEach
    void setUp() {
        calc = new Sm2Calculator();
    }
    
    @Test
    void testAgainRating() {
        // Again(0)은 항상 interval = 1
        Sm2Result result = calc.calculate(
            Sm2Input.builder()
                .rating(0)
                .oldInterval(10.0)
                .oldEaseFactor(2.5)
                .repetition(5)
                .build()
        );
        
        assertEquals(1.0, result.getInterval());
        assertEquals(LocalDate.now().plusDays(1), 
            result.getNextReviewDate());
    }
    
    @Test
    void testGoodRating_InitialCard() {
        // 초기 카드에서 Good 평가
        Sm2Result result = calc.calculate(
            Sm2Input.builder()
                .rating(2)  // Good
                .oldInterval(1.0)
                .oldEaseFactor(2.5)
                .repetition(0)
                .build()
        );
        
        // interval = 1 * 2.36 ≈ 2.36
        // EF = 2.5 + 0.1 = 2.6
        assertEquals(2.36, result.getInterval(), 0.1);
        assertTrue(result.getEaseFactor() > 2.5);
    }
    
    @Test
    void testEasyRating() {
        // Easy(3)는 interval * EF * 1.2
        Sm2Result result = calc.calculate(
            Sm2Input.builder()
                .rating(3)  // Easy
                .oldInterval(2.5)
                .oldEaseFactor(2.4)
                .repetition(1)
                .build()
        );
        
        // interval = 2.5 * 2.4 * 1.2 ≈ 7.2
        assertEquals(7.2, result.getInterval(), 0.1);
    }
    
    @Test
    void testEaseFactorMinimum() {
        // EF는 최소 1.3
        Sm2Result result = calc.calculate(
            Sm2Input.builder()
                .rating(0)  // Again (EF 하락)
                .oldInterval(1.0)
                .oldEaseFactor(1.3)  // 이미 최소값
                .repetition(0)
                .build()
        );
        
        assertTrue(result.getEaseFactor() >= 1.3);
    }
}
```

---

## 예시 6: SM-2 알고리즘을 Card에 통합

**당신의 질문:**
```
SM-2 계산 후 Card 엔티티를 업데이트하는 로직은
어떻게 구현할까?

Controller → Service → SM2Calculator → DB 저장
```

**Claude의 답변 예시:**
```
=== CardService에 추가 ===
@Service @RequiredArgsConstructor
public class CardService {
    private final CardRepository repo;
    private final Sm2Calculator calculator;
    
    public CardResponse reviewCard(UUID cardId, int rating) {
        Card card = repo.findById(cardId)
            .orElseThrow();
        
        // SM-2 계산
        Sm2Result result = calculator.calculate(
            Sm2Input.builder()
                .rating(rating)
                .oldInterval(card.getInterval())
                .oldEaseFactor(card.getEaseFactor())
                .repetition(card.getRepetition())
                .build()
        );
        
        // Card 업데이트
        card.setEaseFactor(result.getEaseFactor());
        card.setInterval(result.getInterval());
        card.setNextReviewDate(result.getNextReviewDate());
        
        if (rating != 0) {  // Again이 아니면
            card.setRepetition(card.getRepetition() + 1);
        } else {
            card.setRepetition(0);  // Again이면 리셋
        }
        
        return toResponse(repo.save(card));
    }
}

=== Controller에 엔드포인트 추가 ===
@PostMapping("/{deckId}/cards/{cardId}/review")
public ResponseEntity<CardResponse> reviewCard(
    @PathVariable UUID deckId,
    @PathVariable UUID cardId,
    @RequestBody RateCardRequest req
) {
    // req.rating = 0~3
    return ResponseEntity.ok(
        cardService.reviewCard(cardId, req.getRating())
    );
}
```

---

# 복습 세션

## 예시 7: 복습 세션 구현 (전체 플로우)

**당신의 질문:**
```
복습 세션 구현:
1. 사용자가 복습 시작 → 세션 생성
2. 오늘 복습할 카드 조회 (nextReviewDate <= today)
3. 카드 하나씩 평가
4. SM-2 계산 후 다음 복습일 업데이트

엔티티, 서비스, 컨트롤러를 어떻게 구성할까?
```

**Claude의 답변 예시:**
```
=== Entity ===
@Entity @Table(name = "review_sessions")
public class ReviewSession {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    private UUID userId;
    private UUID deckId;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private int totalCards;
    private int correctCount;
}

@Entity @Table(name = "review_logs")
public class ReviewLog {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne @JoinColumn(name = "session_id")
    private ReviewSession session;
    
    @ManyToOne @JoinColumn(name = "card_id")
    private Card card;
    
    private int rating;  // 0~3
    private LocalDateTime reviewedAt;
}

=== Service ===
@Service @RequiredArgsConstructor
public class ReviewSessionService {
    private final ReviewSessionRepository sessionRepo;
    private final ReviewLogRepository logRepo;
    private final CardRepository cardRepo;
    private final CardService cardService;
    private final Sm2Calculator calculator;
    
    // 1. 복습 세션 시작
    public ReviewSessionResponse startSession(UUID userId, UUID deckId) {
        ReviewSession session = ReviewSession.builder()
            .userId(userId)
            .deckId(deckId)
            .startedAt(LocalDateTime.now())
            .build();
        return toResponse(sessionRepo.save(session));
    }
    
    // 2. 오늘 복습할 카드 조회
    public List<CardResponse> getTodayCards(UUID deckId) {
        LocalDate today = LocalDate.now();
        return cardRepo.findByDeckIdAndNextReviewDateLessThanEqualAndDeletedAtIsNull(
            deckId, today
        ).stream()
            .map(this::toCardResponse)
            .toList();
    }
    
    // 3. 카드 평가 (SM-2 계산 포함)
    public CardResponse rateCard(
        UUID sessionId,
        UUID cardId,
        int rating
    ) {
        ReviewSession session = sessionRepo.findById(sessionId)
            .orElseThrow();
        Card card = cardRepo.findById(cardId)
            .orElseThrow();
        
        // SM-2 계산
        Sm2Result result = calculator.calculate(
            Sm2Input.builder()
                .rating(rating)
                .oldInterval(card.getInterval())
                .oldEaseFactor(card.getEaseFactor())
                .repetition(card.getRepetition())
                .build()
        );
        
        // ReviewLog 저장
        ReviewLog log = ReviewLog.builder()
            .session(session)
            .card(card)
            .rating(rating)
            .reviewedAt(LocalDateTime.now())
            .build();
        logRepo.save(log);
        
        // Card 업데이트
        card.setEaseFactor(result.getEaseFactor());
        card.setInterval(result.getInterval());
        card.setNextReviewDate(result.getNextReviewDate());
        if (rating != 0) {
            card.setRepetition(card.getRepetition() + 1);
        } else {
            card.setRepetition(0);
        }
        cardRepo.save(card);
        
        // 통계 업데이트
        if (rating >= 2) {  // Good 이상
            session.setCorrectCount(session.getCorrectCount() + 1);
        }
        session.setTotalCards(session.getTotalCards() + 1);
        sessionRepo.save(session);
        
        return toCardResponse(card);
    }
}

=== Repository ===
@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {
    List<Card> findByDeckIdAndNextReviewDateLessThanEqualAndDeletedAtIsNull(
        UUID deckId,
        LocalDate nextReviewDate
    );
}

=== Controller ===
@RestController
@RequestMapping("/api/v1/review-sessions")
public class ReviewSessionController {
    private final ReviewSessionService service;
    
    @PostMapping
    public ResponseEntity<ReviewSessionResponse> start(
        @RequestBody StartSessionRequest req
    ) {
        UUID userId = getCurrentUserId();  // JWT에서 추출
        return ResponseEntity.status(201)
            .body(service.startSession(userId, req.getDeckId()));
    }
    
    @GetMapping("/{sessionId}/cards/today")
    public ResponseEntity<List<CardResponse>> getTodayCards(
        @PathVariable UUID sessionId
    ) {
        ReviewSession session = sessionRepo.findById(sessionId)
            .orElseThrow();
        return ResponseEntity.ok(
            service.getTodayCards(session.getDeckId())
        );
    }
    
    @PostMapping("/{sessionId}/cards/{cardId}/rate")
    public ResponseEntity<CardResponse> rateCard(
        @PathVariable UUID sessionId,
        @PathVariable UUID cardId,
        @RequestBody RateCardRequest req
    ) {
        return ResponseEntity.ok(
            service.rateCard(sessionId, cardId, req.getRating())
        );
    }
}
```

---

# Kafka 이벤트

## 예시 8: Kafka 이벤트 발행

**당신의 질문:**
```
카드를 평가하면 `card.reviewed` Kafka 이벤트를 발행해야 해.
Spring Cloud Stream으로 어떻게 구현할까?

필요한 것:
- CardReviewedEvent 정의
- Kafka Producer
- Topic: card.reviewed
```

**Claude의 답변 예시:**
```
=== dependencies 추가 (build.gradle.kts) ===
implementation("org.springframework.cloud:spring-cloud-stream")
implementation("org.springframework.cloud:spring-cloud-stream-binder-kafka")

=== Event DTO ===
@Data @Builder
public class CardReviewedEvent {
    private String eventId;
    private UUID cardId;
    private UUID deckId;
    private UUID userId;
    private int rating;
    private double newInterval;
    private double newEaseFactor;
    private LocalDateTime reviewedAt;
}

=== Producer (Binder) ===
@Component
public class CardReviewedEventProducer {
    
    // 함수형 프로그래밍 스타일
    @Bean
    public Supplier<CardReviewedEvent> cardReviewedEventSupplier() {
        return () -> null;  // Spring Cloud Stream이 처리
    }
    
    // 또는 StreamBridge 사용
    @RequiredArgsConstructor
    public static class CardEventPublisher {
        private final StreamBridge streamBridge;
        
        public void publishCardReviewed(CardReviewedEvent event) {
            streamBridge.send(
                "cardReviewedEvent-out-0",
                event
            );
        }
    }
}

=== application.yml 설정 ===
spring:
  cloud:
    stream:
      bindings:
        cardReviewedEvent-out-0:
          destination: card.reviewed
          contentType: application/json
      kafka:
        binder:
          brokers: localhost:9092

=== Service에서 발행 ===
@Service
@RequiredArgsConstructor
public class ReviewSessionService {
    private final StreamBridge streamBridge;
    
    public CardResponse rateCard(
        UUID sessionId,
        UUID cardId,
        int rating
    ) {
        // ... (SM-2 계산 및 Card 업데이트)
        
        // Kafka 이벤트 발행
        CardReviewedEvent event = CardReviewedEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .cardId(cardId)
            .deckId(session.getDeckId())
            .userId(session.getUserId())
            .rating(rating)
            .newInterval(result.getInterval())
            .newEaseFactor(result.getEaseFactor())
            .reviewedAt(LocalDateTime.now())
            .build();
        
        streamBridge.send("cardReviewedEvent-out-0", event);
        
        return toCardResponse(card);
    }
}

=== Test (Embedded Kafka) ===
@SpringBootTest
class CardReviewedEventTest {
    
    @Autowired
    private StreamBridge streamBridge;
    
    @Autowired
    private ReviewSessionService service;
    
    @Test
    void testCardReviewedEventPublished() {
        // 1. 복습 세션 생성
        ReviewSession session = createTestSession();
        
        // 2. 카드 평가
        service.rateCard(session.getId(), cardId, 2);
        
        // 3. Kafka 메시지 확인 (TestBinder 사용)
        // 실제 Kafka 없이도 테스트 가능
    }
}
```

---

# 테스트 작성

## 예시 9: JUnit 5 + Testcontainers로 통합테스트

**당신의 질문:**
```
전체 플로우를 통합테스트하고 싶어:
1. 덱 생성
2. 카드 5장 추가
3. 복습 세션 시작
4. 카드 평가
5. DB에 다음 복습일이 올바르게 저장되는지 확인

PostgreSQL은 Docker로 띄우고 싶어. 어떻게 할까?
```

**Claude의 답변 예시:**
```
=== build.gradle.kts 의존성 ===
testImplementation("org.testcontainers:testcontainers:1.19.0")
testImplementation("org.testcontainers:postgresql:1.19.0")
testImplementation("org.springframework.boot:spring-boot-testcontainers")

=== Test Base Class ===
@SpringBootTest
public abstract class IntegrationTestBase {
    
    @Container
    static PostgreSQLContainer<?> postgres = 
        new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("test_db")
            .withUsername("test")
            .withPassword("test");
    
    @DynamicPropertySource
    static void registerPostgresProperties(
        DynamicPropertyRegistry registry
    ) {
        registry.add("spring.datasource.url", 
            postgres::getJdbcUrl);
        registry.add("spring.datasource.username", 
            postgres::getUsername);
        registry.add("spring.datasource.password", 
            postgres::getPassword);
    }
}

=== 실제 Test ===
@SpringBootTest
@Sql({"/data.sql"})  // 테스트 데이터 초기화
class ReviewSessionE2ETest extends IntegrationTestBase {
    
    @Autowired
    private DeckService deckService;
    
    @Autowired
    private CardService cardService;
    
    @Autowired
    private ReviewSessionService sessionService;
    
    @Autowired
    private CardRepository cardRepo;
    
    @Test
    void testCompleteReviewFlow() {
        UUID userId = UUID.randomUUID();
        
        // 1. 덱 생성
        CreateDeckRequest deckReq = new CreateDeckRequest(
            "Java 복습", "OOP 개념", false
        );
        DeckResponse deck = deckService.create(userId, deckReq);
        
        // 2. 카드 5장 추가
        for (int i = 0; i < 5; i++) {
            CreateCardRequest cardReq = new CreateCardRequest(
                "Q" + i,
                "A" + i,
                List.of("java")
            );
            cardService.create(deck.getId(), cardReq);
        }
        
        // 3. 복습 세션 시작
        ReviewSessionResponse session = 
            sessionService.startSession(userId, deck.getId());
        
        // 4. 오늘 복습할 카드 조회
        List<CardResponse> todayCards = 
            sessionService.getTodayCards(deck.getId());
        assertEquals(5, todayCards.size());
        
        // 5. 각 카드 평가
        todayCards.forEach(card -> {
            CardResponse rated = sessionService.rateCard(
                session.getId(),
                card.getId(),
                2  // Good
            );
            
            // SM-2 계산 확인
            assertTrue(rated.getNextReviewDate()
                .isAfter(LocalDate.now()));
        });
        
        // 6. 통계 조회
        ReviewStatistics stats = sessionService
            .getSessionStatistics(session.getId());
        assertEquals(5, stats.getTotalReviews());
        assertEquals(5, stats.getCorrectCount());
        assertEquals(1.0, stats.getAccuracy());
    }
}

=== test-data.sql ===
INSERT INTO decks (id, user_id, title) VALUES (
    '123e4567-e89b-12d3-a456-426614174000',
    '123e4567-e89b-12d3-a456-426614174001',
    'Test Deck'
);
```

---

# 버그 디버깅

## 예시 10: Kafka 이벤트가 발행되지 않을 때

**당신의 질문:**
```
카드를 평가해도 Kafka 이벤트가 발행되지 않아.

에러 메시지:
```
No binding found for StreamBridge#send(cardReviewedEvent-out-0)
```

어디가 잘못된 걸까?
```

**Claude의 답변 예시:**
```
체크리스트:

1. build.gradle.kts에 의존성 있나?
   ✓ spring-cloud-stream
   ✓ spring-cloud-stream-binder-kafka

2. application.yml에 binding 설정 있나?
   ```yaml
   spring:
     cloud:
       stream:
         bindings:
           cardReviewedEvent-out-0:  ← 이 이름이 정확해야 함
             destination: card.reviewed
   ```

3. StreamBridge 주입 확인:
   ```java
   @Autowired
   private StreamBridge streamBridge;  ← 빈 등록 확인
   ```

4. 발행 코드 확인:
   ```java
   streamBridge.send("cardReviewedEvent-out-0", event);
   ```
   여기서 첫 번째 인자가 application.yml의 binding 이름과 
   정확히 일치해야 함.

5. 아직도 안 되면, 임시로 로그 추가:
   ```java
   log.info("Publishing event: {}", event);
   streamBridge.send("cardReviewedEvent-out-0", event);
   log.info("Event published successfully");
   ```
```

---

## 예시 11: SM-2 계산이 이상할 때

**당신의 질문:**
```
카드를 "Good"으로 평가했는데 다음 복습일이 
너무 가까워 (1일 뒤).

현재 값:
- interval: 1.0
- EF: 2.5
- rating: Good(2)

계산 결과:
- newInterval: 2.5
- nextReviewDate: 2026-05-15 (내일)

맞나? 아니면 버그?
```

**Claude의 답변 예시:**
```
맞는 계산이야!

Good(2)일 때:
newInterval = oldInterval * newEF
            = 1.0 * 2.5
            = 2.5일

nextReviewDate = today + 2.5days
               = 2026-05-14 + 2.5일
               ≈ 2026-05-16 또는 2026-05-17

아! 버그 발견:
LocalDate.now().plusDays((long) 2.5)
                          ↑ 이게 문제!

소수점이 버려지니까:
plusDays(2)  ← 2.5 → 2로 변환됨

해결책:
```java
long daysToAdd = Math.round(newInterval);
LocalDate nextDate = LocalDate.now().plusDays(daysToAdd);
```

또는:

```java
// Duration 사용 (시간 단위도 지원)
Duration duration = Duration.ofHours((long) (newInterval * 24));
LocalDateTime nextDateTime = LocalDateTime.now().plus(duration);
```

둘 중 어느 방식이 좋을까요?
```

---

## 예시 12: 페이지네이션 테스트

**당신의 질문:**
```
GET /api/v1/decks?page=0&size=20
이 API를 테스트하는데,
응답 형식이 뭔지 모르겠어.

Spring Data의 Page<> 응답이 어떻게 생길까?
```

**Claude의 답변 예시:**
```
Spring Data Page<> 응답 형식:
```json
{
  "content": [
    {
      "id": "uuid",
      "title": "Deck 1",
      ...
    },
    ...
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": {...}
  },
  "totalElements": 42,
  "totalPages": 3,
  "first": true,
  "last": false,
  "hasNext": true,
  "hasPrevious": false,
  "isEmpty": false
}
```

테스트:
```java
@Test
void testDeckPagination() {
    // 데이터 42개 삽입
    
    ResponseEntity<Page<DeckResponse>> response = 
        client.get().uri("/api/v1/decks?page=0&size=20")
            .exchange();
    
    Page<DeckResponse> page = response.getBody();
    
    assertEquals(20, page.getContent().size());
    assertEquals(42, page.getTotalElements());
    assertEquals(3, page.getTotalPages());
    assertTrue(page.hasNext());
    assertFalse(page.hasPrevious());
}
```
```

---

## 예시 13: 권한 (userId) 확인 누락

**당신의 질문:**
```
유저 A가 유저 B의 카드를 삭제할 수 있어.
이건 보안 버그인데, 어디서 확인해야 할까?

GET /api/v1/decks/{deckId}
POST /api/v1/decks/{deckId}/cards
DELETE /api/v1/decks/{deckId}/cards/{cardId}
```

**Claude의 답변 예시:**
```
모든 엔드포인트에서 userId 확인 필수!

Service 메서드마다:

```java
public DeckResponse getDeck(UUID deckId) {
    Deck deck = deckRepo.findById(deckId)
        .orElseThrow(() -> new NotFoundException());
    
    // ← 추가 필요!
    if (!deck.getUserId().equals(getCurrentUserId())) {
        throw new ForbiddenException("Not your deck");
    }
    
    return toResponse(deck);
}

private UUID getCurrentUserId() {
    // JWT Token에서 추출
    return jwtUtil.extractUserId(
        SecurityContextHolder.getContext()
            .getAuthentication()
    );
}
```

모든 조회/수정/삭제에 이 체크 추가!

테스트:
```java
@Test
void testUnauthorizedDelete() {
    UUID otherUserId = UUID.randomUUID();
    Deck deck = createDeckForUser(otherUserId);
    
    // 내 userId로 요청
    assertThrows(ForbiddenException.class, 
        () -> cardService.delete(
            deck.getId(),
            cardId
        )
    );
}
```
```

---

# 🎯 빠른 참고용 체크리스트

각 Step마다 아래를 체크하세요:

## Step 1: 초기 설정
```
□ Spring Boot 프로젝트 생성
□ Modulith 의존성 추가
□ card, srs 모듈 생성
□ ApplicationModules.verify() 통과
□ Health endpoint 정상
□ Docker Compose로 PostgreSQL 실행
```

## Step 2: 덱/카드 CRUD
```
□ Deck, Card Entity 작성
□ Repository 작성
□ Service 비즈니스 로직
□ Controller 6개 API 구현
□ Flyway 마이그레이션
□ 통합테스트 작성
□ 입력값 검증 (Bean Validation)
```

## Step 3: SM-2
```
□ Sm2Calculator 서비스
□ Sm2Input, Sm2Result DTO
□ 단위테스트 12개
□ Card에 SM-2 필드 추가
□ review_logs 테이블
□ POST /cards/{cardId}/review 엔드포인트
```

## Step 4: 복습 세션
```
□ ReviewSession Entity
□ ReviewLog Entity
□ 복습 세션 시작 API
□ 오늘 카드 조회 API
□ 카드 평가 API (SM-2 통합)
□ 통합테스트
□ 통계 API
```

## Step 5: Kafka
```
□ CardReviewedEvent DTO
□ StreamBridge로 발행
□ application.yml 설정
□ Embedded Kafka 테스트
```

---

**더 궁금한 점이 있으면 이 파일의 예시를 참고하고**
**Claude에게 물어보세요!**

당신은 할 수 있어! 💪
