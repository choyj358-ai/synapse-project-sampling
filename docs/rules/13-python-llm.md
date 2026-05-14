# 13. Python + LLM RULE — FastAPI & AI

> **모듈**: `synapse-learning-svc/learning-ai/` · Python 3.11 · FastAPI · Pydantic v2 · Anthropic SDK · OpenAI SDK  
> **준수 수준**: [MUST] 반드시 / [SHOULD] 권장 / [MAY] 선택

---

## 13.1 FastAPI 엔드포인트 [MUST]

Pydantic v2 모델, async 엔드포인트, 타입 힌트 100% 필수야.

```python
# ✅ Good
class GenerateCardRequest(BaseModel):
    model_config = ConfigDict(strict=True)
    note_id: str
    max_cards: int = 5

class GenerateCardResponse(BaseModel):
    cards: list[dict[str, str]]
    tokens_used: int

@router.post("/generate-cards", response_model=GenerateCardResponse)
async def generate_cards(req: GenerateCardRequest) -> GenerateCardResponse:
    result = await card_service.generate(note_id=req.note_id)
    return GenerateCardResponse(cards=result.cards, tokens_used=result.tokens_used)
```

```python
# ❌ Bad — sync + dict 타입 + 리턴 힌트 없음
@router.post("/generate-cards")
def generate_cards(request: dict):
    return {"cards": card_service.generate(request["note_id"])}
```

> **이유**: async 안 쓰면 LLM I/O에서 이벤트 루프가 블로킹돼. Pydantic v2 없으면 타입 에러가 런타임까지 숨어.

---

## 13.2 LLM 호출 [MUST]

timeout 30초, retry 3회 + exponential backoff. Anthropic/OpenAI 동일 패턴 적용해.

```python
# ✅ Good — Anthropic SDK
from anthropic import AsyncAnthropic, RateLimitError, APIConnectionError
from tenacity import retry, stop_after_attempt, wait_exponential, retry_if_exception_type

client = AsyncAnthropic(timeout=httpx.Timeout(30.0, connect=5.0))

@retry(
    stop=stop_after_attempt(3),
    wait=wait_exponential(multiplier=1, min=2, max=10),
    retry=retry_if_exception_type((RateLimitError, APIConnectionError)),
)
async def call_anthropic(system_prompt: str, user_message: str) -> str:
    response = await client.messages.create(
        model="claude-sonnet-4-20250514", max_tokens=2048,
        system=system_prompt,
        messages=[{"role": "user", "content": user_message}],
    )
    return response.content[0].text
```

OpenAI SDK도 동일 패턴 (`AsyncOpenAI(timeout=...)` + `@retry`) 적용해.

```python
# ❌ Bad — timeout 없음, sync, retry 없음
client = Anthropic()
def call_llm(prompt: str) -> str:
    return client.messages.create(model="claude-sonnet-4-20250514", max_tokens=2048,
        messages=[{"role": "user", "content": prompt}]).content[0].text
```

> **이유**: LLM API는 레이턴시 분산이 크고 rate limit도 자주 걸려. timeout + retry 없으면 한 요청이 서비스 전체를 먹통으로 만들어.

---

## 13.3 프롬프트 관리 [SHOULD]

system/user 메시지 분리, `prompts/` 디렉토리에서 파일 기반 버전 관리해.

디렉토리 구조: `prompts/{task}/system.txt` + `prompts/{task}/user.jinja2`

```python
# ✅ Good — 파일 기반 프롬프트 로딩
from pathlib import Path
from functools import lru_cache
from jinja2 import Template

PROMPTS_DIR = Path(__file__).parent.parent / "prompts"

@lru_cache(maxsize=32)
def load_system_prompt(task: str) -> str:
    return (PROMPTS_DIR / task / "system.txt").read_text(encoding="utf-8")

def render_user_prompt(task: str, **kwargs: str) -> str:
    tmpl = Template((PROMPTS_DIR / task / "user.jinja2").read_text(encoding="utf-8"))
    return tmpl.render(**kwargs)
```

```python
# ❌ Bad — 프롬프트 하드코딩
response = await client.messages.create(
    system="너는 학습카드를 만드는 AI야. 핵심 개념을 추출해서...(200자)",
    messages=[{"role": "user", "content": note_content}], ...)
```

> **이유**: 프롬프트는 코드보다 자주 바뀌어. 파일로 분리하면 PR diff로 변경 이력이 보이고, 비개발자도 수정 가능해.

---

## 13.4 비용 제어 — 토큰 로깅 [MUST] + 일일 한도 [SHOULD]

```python
# ✅ Good — 데코레이터로 자동 토큰 추적
import functools, logging
from datetime import date

logger = logging.getLogger("llm.cost")
_daily_tokens: dict[str, int] = {}   # 프로덕션에선 Redis
DAILY_LIMIT = 500_000

def track_tokens(func):
    @functools.wraps(func)
    async def wrapper(*args, **kwargs):
        today = date.today().isoformat()
        if _daily_tokens.get(today, 0) >= DAILY_LIMIT:
            raise TokenLimitExceededError(f"일일 한도 {DAILY_LIMIT} 초과")
        response = await func(*args, **kwargs)
        total = response.usage.input_tokens + response.usage.output_tokens
        _daily_tokens[today] = _daily_tokens.get(today, 0) + total
        logger.info("LLM call", extra={
            "model": response.model, "input_tokens": response.usage.input_tokens,
            "output_tokens": response.usage.output_tokens, "daily_total": _daily_tokens[today],
        })
        return response
    return wrapper
```

```python
# ❌ Bad — usage 정보를 버림, 비용 추적 불가
async def call_llm(prompt: str) -> str:
    response = await client.messages.create(...)
    return response.content[0].text   # usage 무시
```

> **이유**: 로깅 없으면 어떤 기능이 비용을 먹는지 모르고, 한도 없으면 버그 하나로 월 청구서가 폭발해.

---

## 13.5 테스트 [MUST]

CI에서 실제 LLM API 호출 절대 금지. mock 필수야.

```python
# ✅ Good — pytest fixture로 Anthropic mock
@pytest.fixture
def mock_anthropic_client():
    mock_resp = Message(
        id="msg_test", type="message", role="assistant",
        content=[ContentBlock(type="text", text="학습카드 내용")],
        model="claude-sonnet-4-20250514",
        usage=Usage(input_tokens=100, output_tokens=50), stop_reason="end_turn",
    )
    with patch("src.llm_client.client") as m:
        m.messages.create = AsyncMock(return_value=mock_resp)
        yield m

@pytest.mark.asyncio
async def test_generate_cards(mock_anthropic_client):
    result = await call_anthropic("시스템", "유저 메시지")
    assert result == "학습카드 내용"
    mock_anthropic_client.messages.create.assert_called_once()
```

`httpx.MockTransport`로 HTTP 레벨 mock도 가능해 (`httpx.AsyncClient(transport=MockTransport(handler))`).

```python
# ❌ Bad — CI에서 실제 API 호출
async def test_generate():
    result = await call_anthropic("...", "테스트")  # 비결정적 + 비용 발생
    assert len(result) > 0
```

> **이유**: 실제 호출하면 CI가 비결정적이고, 비용도 나가고, API 키 노출 위험까지 있어.

---

## 13.6 에러 처리 [MUST]

에러 종류별 분류 처리해. fallback은 [MAY]야.

- `RateLimitError` / `APIConnectionError` → retry (13.2 패턴)
- `AuthenticationError` → 즉시 실패 + 알림
- `BadRequestError` → 400 반환
- Anthropic 실패 → OpenAI fallback [MAY]

```python
# ✅ Good — 분류 처리 + fallback
async def call_with_fallback(system: str, user: str) -> str:
    try:
        return await call_anthropic(system, user)
    except AuthenticationError:
        logger.critical("Anthropic 인증 실패")
        raise HTTPException(503, detail="AI 서비스 불가")
    except BadRequestError as e:
        raise HTTPException(400, detail=f"잘못된 요청: {e}")
    except (RateLimitError, APIConnectionError) as e:
        logger.warning(f"Anthropic 실패, fallback: {e}")
        try:
            return await call_openai(system, user)
        except Exception:
            raise HTTPException(503, detail="AI 서비스 불가")
```

```python
# ❌ Bad — 모든 에러를 삼킴
async def call_llm(prompt: str) -> str:
    try:
        return await client.messages.create(...)
    except Exception:
        return ""   # 디버깅 불가, 빈 응답
```

> **이유**: 인증 에러에 retry 하면 의미 없고, rate limit에 즉시 실패하면 UX가 나빠져. 에러마다 대응이 달라야 해.

---

## 13.7 pyproject.toml [SHOULD]

ruff + mypy 설정을 pyproject.toml 하나에 통합해.

```toml
# ✅ Good
[project]
name = "synapse-learning-ai"
requires-python = ">=3.11"
dependencies = ["fastapi>=0.110.0", "pydantic>=2.6.0", "anthropic>=0.40.0",
    "openai>=1.50.0", "httpx>=0.27.0", "tenacity>=8.2.0", "jinja2>=3.1.0"]
[project.optional-dependencies]
dev = ["pytest>=8.0", "pytest-asyncio>=0.23", "ruff>=0.5.0", "mypy>=1.10"]

[tool.ruff]
target-version = "py311"
line-length = 100
[tool.ruff.lint]
select = ["E", "F", "I", "N", "UP", "ANN", "B", "SIM"]

[tool.mypy]
python_version = "3.11"
strict = true
[[tool.mypy.overrides]]
module = ["anthropic.*", "openai.*"]
ignore_missing_imports = true
```

```
# ❌ Bad — 설정 파일 난립
├── .flake8      ├── mypy.ini
├── setup.cfg    ├── requirements.txt
```

> **이유**: 하나로 통합하면 설정 충돌 없고, ruff가 flake8+isort+pyupgrade를 대체해서 도구 수도 줄어.

---

## 요약 체크리스트

| # | 규칙 | 수준 |
|---|------|------|
| 13.1 | FastAPI — Pydantic v2, async, 타입 힌트 100% | [MUST] |
| 13.2 | LLM 호출 — timeout 30s, retry 3회 exponential backoff | [MUST] |
| 13.3 | 프롬프트 관리 — 파일 분리, 버전 관리 | [SHOULD] |
| 13.4 | 비용 제어 — 토큰 로깅 [MUST] + 일일 한도 [SHOULD] | [MUST]/[SHOULD] |
| 13.5 | 테스트 — LLM mock, CI에서 실제 호출 금지 | [MUST] |
| 13.6 | 에러 처리 — 분류 처리 + fallback [MAY] | [MUST] |
| 13.7 | pyproject.toml — ruff + mypy 통합 | [SHOULD] |

_마지막 업데이트: 2026-05-12_
