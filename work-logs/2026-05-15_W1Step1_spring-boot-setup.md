# W1 Step 1 — learning-card 골격 생성

**날짜**: 2026-05-15  
**담당**: @learning-card-owner  
**소요 시간**: 0.5일

---

## 생성된 파일 목록

| 파일 | 작업 |
|------|------|
| `learning-card/src/main/resources/application.properties` | 수정 — port 8082, actuator 설정 |
| `learning-card/src/main/java/com/synapse/learning/card/package-info.java` | 신규 — @ApplicationModule |
| `learning-card/src/main/java/com/synapse/learning/card/CardController.java` | 신규 — 빈 Controller stub |
| `learning-card/src/main/java/com/synapse/learning/card/CardService.java` | 신규 — 빈 Service stub |
| `learning-card/src/main/java/com/synapse/learning/srs/package-info.java` | 신규 — @ApplicationModule |
| `learning-card/src/main/java/com/synapse/learning/srs/SrsService.java` | 신규 — 빈 Service stub |
| `learning-card/Dockerfile` | 신규 — multi-stage build (eclipse-temurin:21-jdk → jre) |
| `docker-compose.yml` | 신규 — 루트 공통 관리, learning-card 서비스 정의 |

---

## 테스트 결과

```
BUILD SUCCESSFUL in 28s
7 actionable tasks: 7 executed
```

- `ModuleStructureTest.verifyModuleStructure()` 통과
- `./gradlew build` 성공

---

## 모듈 구조

```
com.synapse.learning
├── LearningCardApplication.java   (@Modulithic)
├── card/
│   ├── package-info.java          (@ApplicationModule)
│   ├── CardController.java        (stub)
│   └── CardService.java           (stub)
└── srs/
    ├── package-info.java          (@ApplicationModule)
    └── SrsService.java            (stub)
```

- **card 모듈**: 덱/카드 CRUD 담당 (Step 2에서 구현)
- **srs 모듈**: SM-2 알고리즘·복습 스케줄 담당 (Step 3에서 구현)

---

## 학습 내용

- Spring Modulith에서 모듈은 `@ApplicationModule`을 `package-info.java`에 선언하면 자동 감지됨
- `ApplicationModules.of(App.class).verify()`로 모듈 경계 위반을 빌드 시점에 검출 가능
- multi-stage Dockerfile: JDK로 빌드 후 JRE만 포함된 경량 이미지로 실행 → 이미지 크기 절감

---

## 특이사항

- Docker 이미지 빌드 및 `docker compose up` 실행은 Docker 환경에서 별도 확인 필요
- `GET /actuator/health` 런타임 확인은 서버 기동 시 확인 필요
