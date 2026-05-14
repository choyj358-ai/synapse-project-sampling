# 7.3 Flutter 3 + Riverpod RULE — Platform Flutter

> **참조**: [전체 Rule 목록](../rules/) | [DESIGN.md](../../DESIGN.md)

---

## 7.3.1 상태 관리 [MUST]

Riverpod 3 + `@riverpod` 코드 생성만 써. `setState()`, `ChangeNotifier`, `ValueNotifier` 전부 금지.
모든 비동기 상태는 `AsyncValue`로 표현하고 `.when()`으로 분기해.

| 상황 | Provider 타입 |
|------|--------------|
| 단순 계산 | `Provider` |
| 1회성 비동기 | `FutureProvider` |
| 실시간 스트림 | `StreamProvider` |
| 변경 가능 + 액션 | `NotifierProvider` |
| ID별 인스턴스 | `.family` modifier |
| 화면 이탈 시 해제 | `autoDispose` |

```dart
// ✅ Good — @riverpod 코드 생성
@riverpod
Future<List<Note>> noteList(NoteListRef ref) async {
  final repository = ref.watch(noteRepositoryProvider);
  return repository.fetchNotes();
}

@riverpod
class NoteEditor extends _$NoteEditor {
  @override
  NoteEditorState build(String noteId) => NoteEditorState.loading();

  void updateTitle(String title) {
    if (state case NoteEditorState(:final loaded?)) {
      state = loaded.copyWith(title: title, isDirty: true);
    }
  }
}
```

```dart
// ❌ Bad — setState 사용
class _NoteCardState extends State<NoteCard> {
  String title = '';
  Future<void> _fetch() async {
    final note = await NoteApi.getNote(widget.noteId);
    setState(() { title = note.title; });  // 금지! Riverpod으로 대체
  }
}
```

> **이유**: Riverpod은 의존성 그래프 자동 관리, `autoDispose` 메모리 누수 방지, `overrideWith`로 mock 주입이 간편해. `setState()`는 상태가 위젯에 묶여서 재사용/테스트 불가, 리빌드 범위 제어 불가.

---

## 7.3.2 라우팅 [SHOULD]

GoRouter 14 사용, 모든 경로는 `AppRoutes` 상수 클래스에서 중앙 관리해.
딥링크/웹 URL 동기화 기본 지원. `Navigator.push()` 직접 호출 금지.

```dart
// core/constants/app_routes.dart — 경로 상수 중앙화
abstract class AppRoutes {
  static const login = '/login';
  static const home = '/';
  static const notes = '/notes';
  static const noteDetail = '/notes/:noteId';
  static const cards = '/cards';
  static const graph = '/graph';
  static String noteDetailPath(String noteId) => '/notes/$noteId';
}

// GoRouter 설정 — 인증 redirect + ShellRoute
final routerProvider = Provider<GoRouter>((ref) {
  final authState = ref.watch(authStateProvider);
  return GoRouter(
    initialLocation: AppRoutes.home,
    redirect: (context, state) {
      final isLoggedIn = authState.isAuthenticated;
      if (!isLoggedIn && state.matchedLocation != AppRoutes.login) return AppRoutes.login;
      if (isLoggedIn && state.matchedLocation == AppRoutes.login) return AppRoutes.home;
      return null;
    },
    routes: [
      GoRoute(path: AppRoutes.login, builder: (_, __) => const LoginScreen()),
      ShellRoute(
        builder: (_, state, child) => MainShell(child: child),
        routes: [
          GoRoute(path: AppRoutes.home, builder: (_, __) => const DashboardScreen()),
          GoRoute(path: AppRoutes.notes, builder: (_, __) => const NoteListScreen()),
          GoRoute(path: AppRoutes.cards, builder: (_, __) => const CardListScreen()),
        ],
      ),
    ],
  );
});
```

```dart
// ✅ Good                                    // ❌ Bad
context.go(AppRoutes.noteDetailPath(note.id)); // context.go('/notes/${note.id}'); ← 하드코딩!
context.push(AppRoutes.noteEditorPath(id));    // Navigator.push(...); ← GoRouter 안 씀!
```

> **이유**: GoRouter는 웹 URL 동기화 + 딥링크 + redirect 가드 선언적 처리. `Navigator.push()`는 URL 안 바뀌고 뒤로가기 망가짐.

---

## 7.3.3 디렉토리 [MUST]

**Feature-first** 구조. 기능별 `data/domain/presentation/providers` 자체 보유, 횡단 import 금지.

```
lib/
├── main.dart                     # ProviderScope 래핑
├── app.dart                      # MaterialApp + GoRouter + ThemeData
├── core/
│   ├── constants/                # app_colors / app_spacing / app_text_styles / app_routes
│   ├── theme/                    # Light/Dark ThemeData
│   ├── network/                  # Dio + AuthInterceptor + TenantInterceptor
│   ├── error/                    # AppException, Result<T>
│   └── utils/
├── shared/
│   ├── widgets/                  # AppErrorWidget, AppSkeleton 등
│   └── models/                   # User, Pagination 등
├── features/                     # 기능별 모듈
│   ├── auth/
│   │   ├── data/                 # Repository 구현 + DTO
│   │   ├── domain/               # 모델 + Repository 인터페이스
│   │   ├── presentation/         # Screen + Widget
│   │   └── providers/            # Riverpod Provider
│   ├── notes/
│   ├── cards/
│   ├── graph/
│   └── dashboard/
└── l10n/                         # 국제화 (ko, en)
```

- **Feature 자기 완결**: data/domain/presentation/providers 자체 보유
- **횡단 참조 금지**: `features/notes/`에서 `features/cards/data/` import 불가
- **공유 필요 시**: `shared/`로 올리거나 Provider 구독
- **3단계 `../` 금지**: `package:synapse/` 절대경로 사용

```dart
// ✅ Good                                    // ❌ Bad
import '../providers/note_list_provider.dart'; // import 'package:synapse/features/cards/data/...'; ← 횡단!
import 'package:synapse/shared/models/user.dart'; // import '../../../core/...'; ← 경로 지옥
```

> **이유**: 횡단 참조 허용하면 순환 의존성 + 변경 파급 효과 폭발.

---

## 7.3.4 디자인 토큰 [MUST]

`AppColors`, `AppSpacing`, `AppTextStyles`, `AppRadius` 상수 클래스로만 스타일 값 접근해.
하드코딩 `Color()`, 매직 넘버, 인라인 `TextStyle` 전부 금지.

### Synapse Warm Intellectual 핵심 토큰

| 토큰 | 값 | 용도 |
|------|-----|------|
| `primaryAmber` | `#D97706` | CTA, 선택, 진행률 |
| `primaryHover` | `#B45309` | 호버/프레스 |
| `primaryLight` | `#FEF3C7` | 배경 하이라이트 |
| `secondaryTeal` | `#0D9488` | 성공, SRS 정답 |
| `stone50`~`950` | Warm Stone | Neutral 전체 |
| `error/warning/success` | 시맨틱 컬러 | DESIGN.md 참조 |

```dart
// ✅ Good — AppColors/AppSpacing/AppTextStyles 상수 사용
Container(
  color: AppColors.stone50,
  padding: const EdgeInsets.all(AppSpacing.md),
  child: Text('노트', style: AppTextStyles.h3.copyWith(color: AppColors.stone900)),
)
// XP 프로그레스 바
LinearProgressIndicator(value: progress, backgroundColor: AppColors.stone200,
  valueColor: AlwaysStoppedAnimation(AppColors.primaryAmber));
```

```dart
// ❌ Bad — 하드코딩
Container(color: Color(0xFFD97706), padding: EdgeInsets.all(16),    // → AppColors.primaryAmber + AppSpacing.md
  child: Text('노트', style: TextStyle(fontSize: 20)));             // → AppTextStyles.h3
Card(color: Colors.white)                                           // → AppColors.stone50!
```

> **이유**: DESIGN.md 변경 시 한 곳만 수정. `Colors.white`/`Colors.grey`는 Warm Stone 톤과 안 맞아.

---

## 7.3.5 분석 [MUST]

`flutter analyze` 경고 **0건**. CI에서 1건이라도 있으면 빌드 실패.

### analysis_options.yaml 핵심 설정

```yaml
include: package:flutter_lints/flutter.yaml

analyzer:
  strong-mode:
    implicit-casts: false       # dynamic 캐스트 차단
    implicit-dynamic: false     # 타입 추론 강제
  exclude: ["**/*.g.dart", "**/*.freezed.dart"]

linter:
  rules:
    - always_declare_return_types
    - avoid_dynamic_calls
    - avoid_print
    - prefer_const_constructors
    - prefer_final_locals
    - require_trailing_commas
```

```dart
// ✅ Good — const + 타입 명시 + trailing comma
class NoteCard extends ConsumerWidget {
  const NoteCard({super.key, required this.noteId});
  final String noteId;
  @override
  Widget build(BuildContext context, WidgetRef ref) => const SizedBox(height: AppSpacing.md);
}
```

```dart
// ❌ Bad — 경고 4건+
class noteCard extends ConsumerWidget {  // PascalCase 위반
  noteCard({required this.noteId});      // const+key 누락
  final noteId;                          // 타입 누락 (implicit-dynamic)
  @override
  Widget build(BuildContext context, WidgetRef ref) {
    print('build');                       // avoid_print
    return SizedBox(height: 16);         // const 누락
  }
}
```

> **이유**: `implicit-casts/dynamic: false`로 `dynamic` NPE 원천 차단. 코드 생성 파일은 exclude 필수.

---

## 7.3.6 네트워크 [SHOULD]

Dio 5 + interceptor 패턴. `baseUrl` 환경별 분리, 토큰 갱신 자동 처리.
Provider에서 Dio 직접 호출 금지 — `Repository -> DataSource -> Dio` 3계층.

```dart
// core/network/dio_client.dart — 환경별 baseUrl + interceptor 등록
final dioProvider = Provider<Dio>((ref) {
  final baseUrl = switch (ref.watch(environmentProvider)) {
    AppEnvironment.dev     => 'http://localhost:8080',
    AppEnvironment.staging => 'https://api-staging.synapse.app',
    AppEnvironment.prod    => 'https://api.synapse.app',
  };
  final dio = Dio(BaseOptions(baseUrl: baseUrl, connectTimeout: const Duration(seconds: 10)));
  dio.interceptors.addAll([AuthInterceptor(ref), TenantInterceptor(ref)]);
  return dio;
});
```

```dart
// ✅ Good — 토큰 자동 갱신 interceptor
class AuthInterceptor extends Interceptor {
  final Ref ref;
  AuthInterceptor(this.ref);

  @override
  void onRequest(RequestOptions options, RequestInterceptorHandler handler) {
    final token = ref.read(authStateProvider).accessToken;
    if (token != null) options.headers['Authorization'] = 'Bearer $token';
    handler.next(options);
  }

  @override
  void onError(DioException err, ErrorInterceptorHandler handler) async {
    if (err.response?.statusCode != 401) return handler.next(err);
    final ok = await ref.read(authStateProvider.notifier).refreshToken();
    if (!ok) { ref.read(authStateProvider.notifier).logout(); return handler.next(err); }
    err.requestOptions.headers['Authorization'] = 'Bearer ${ref.read(authStateProvider).accessToken}';
    handler.resolve(await Dio().fetch(err.requestOptions));
  }
}
```

```dart
// ❌ Bad — Provider에서 Dio 직접 + 토큰 수동 삽입
@riverpod
Future<List<Note>> noteList(NoteListRef ref) async {
  final dio = ref.read(dioProvider);
  final token = ref.read(authStateProvider).accessToken;
  final response = await dio.get('/api/v1/notes',
    options: Options(headers: {'Authorization': 'Bearer $token'}));  // 매번 수동!
  return (response.data['data'] as List).map((j) => Note.fromJson(j)).toList();
}
```

> **이유**: Interceptor로 인증 헤더 + 401 갱신 중앙화. `baseUrl` 환경 분리는 프로덕션 실수 호출 방지.

---

## 7.3.7 테스트 [SHOULD]

화면(Screen)마다 **Widget test 최소 1건**. Provider 로직 복잡하면 unit test 추가.
Golden test는 [MAY] — 핵심 화면에 선택 적용.

```dart
// ✅ Good — Widget test: ProviderScope override + given/when/then
testWidgets('should display note title when loaded', (tester) async {
  final testNote = Note(id: 'n1', title: '테스트 노트', content: '내용');
  await tester.pumpWidget(ProviderScope(
    overrides: [noteProvider('n1').overrideWith((ref, id) => Future.value(testNote))],
    child: const MaterialApp(home: NoteDetailScreen(noteId: 'n1')),
  ));
  await tester.pumpAndSettle();
  expect(find.text('테스트 노트'), findsOneWidget);
});

// ✅ Good — Provider unit test
test('should mark dirty when title updated', () async {
  final container = ProviderContainer(
    overrides: [noteRepositoryProvider.overrideWithValue(MockNoteRepository())],
  );
  addTearDown(container.dispose);
  container.read(noteEditorProvider('n1').notifier).updateTitle('새 제목');
  expect(container.read(noteEditorProvider('n1')).isDirty, true);
});

// ✅ Good [MAY] — Golden test (핵심 화면만)
await expectLater(find.byType(DashboardScreen), matchesGoldenFile('goldens/dashboard.png'));
```

테스트 네이밍: `'should [행동] when [조건]'`

```dart
// ❌ Bad — 모호한 이름 + 여러 시나리오 혼합
testWidgets('test notes', (tester) async { /* 생성+수정+삭제 한꺼번에 → 분리해 */ });
```

> **이유**: `ProviderScope.overrides`로 네트워크 없이 loading/data/error 전부 테스트 가능. Golden test는 디자인 토큰 변경 시 UI 깨짐 캐치에 유용하지만 유지보수 비용 있으니 핵심 화면만.

---

## 요약 체크리스트

| # | 규칙 | 레벨 | 핵심 |
|---|------|------|------|
| 7.3.1 | 상태 관리 | [MUST] | Riverpod 3 + @riverpod only. setState 금지 |
| 7.3.2 | 라우팅 | [SHOULD] | GoRouter 14 + AppRoutes 상수. Navigator 금지 |
| 7.3.3 | 디렉토리 | [MUST] | Feature-first. 횡단 import 금지 |
| 7.3.4 | 디자인 토큰 | [MUST] | AppColors/AppSpacing만. 하드코딩 금지 |
| 7.3.5 | 분석 | [MUST] | flutter analyze 경고 0건 |
| 7.3.6 | 네트워크 | [SHOULD] | Dio 5 + interceptor. 환경별 baseUrl |
| 7.3.7 | 테스트 | [SHOULD] | Widget test 1건/화면. Golden [MAY] |
