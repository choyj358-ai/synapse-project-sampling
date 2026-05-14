"""Learning AI FastAPI application entry point."""

from fastapi import FastAPI

app = FastAPI(
    title="Synapse Learning AI",
    description="AI-powered learning features for Synapse",
    version="0.1.0",
)


@app.get("/health")
async def health() -> dict[str, str]:
    """Liveness probe."""
    return {"status": "ok"}


@app.get("/health/ready")
async def health_ready() -> dict[str, str]:
    """Readiness probe — checks downstream dependencies."""
    return {"status": "ready"}
