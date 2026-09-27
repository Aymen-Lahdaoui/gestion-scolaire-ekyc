from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
import logging
from app.config import settings
from app.routers import ekyc, recognition, liveness

# Configuration des logs
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s"
)
logger = logging.getLogger("ai_service")

app = FastAPI(
    title="Plateforme Scolaire - Service IA & Vision Biométrique",
    description="API FastAPI dédiée à l'eKYC (OCR CIN + Liveness MediaPipe + ArcFace) et à la reconnaissance faciale pour l'émargement temps réel.",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc"
)

# Configuration CORS pour autoriser les requêtes React frontend et Spring Boot
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Enregistrement des routeurs
app.include_router(ekyc.router, prefix=settings.API_V1_STR)
app.include_router(recognition.router, prefix=settings.API_V1_STR)
app.include_router(liveness.router, prefix=settings.API_V1_STR)

@app.get("/health", tags=["Santé"])
async def health_check():
    """Point de terminaison de vérification de santé du service IA."""
    return {
        "status": "healthy",
        "service": settings.APP_NAME,
        "model": settings.MODEL_NAME,
        "detector": settings.DETECTOR_BACKEND,
        "pgvector_search": "enabled"
    }

@app.get("/", tags=["Racine"])
async def root():
    return {
        "message": "Bienvenue sur le Service IA & Vision Biométrique de la Plateforme Scolaire.",
        "documentation": "/docs"
    }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app.main:app", host=settings.HOST, port=settings.PORT, reload=True)
