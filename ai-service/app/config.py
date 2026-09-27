import os
from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    APP_NAME: str = "Service IA - Vision & eKYC"
    API_V1_STR: str = "/api/v1"
    PORT: int = 8000
    HOST: str = "0.0.0.0"
    
    # Database (PostgreSQL with pgvector)
    DB_HOST: str = os.getenv("DB_HOST", "postgres")
    DB_PORT: int = int(os.getenv("DB_PORT", "5432"))
    DB_NAME: str = os.getenv("DB_NAME", "gestion_scolaire")
    DB_USER: str = os.getenv("DB_USER", "admin")
    DB_PASSWORD: str = os.getenv("DB_PASSWORD", "admin123")
    
    # DeepFace & Model Configuration
    MODEL_NAME: str = "ArcFace"
    DETECTOR_BACKEND: str = "opencv"
    SIMILARITY_METRIC: str = "cosine"
    
    # Thresholds
    FACE_MATCH_DISTANCE_THRESHOLD: float = 0.40  # Cosine distance (< 0.40 = match)
    EKYC_AUTO_VALIDATE_SCORE: float = 80.0
    EKYC_MANUAL_REVIEW_SCORE: float = 60.0
    
    # Tesseract path (if needed on Windows / Linux container default is /usr/bin/tesseract)
    TESSERACT_CMD: str = os.getenv("TESSERACT_CMD", "tesseract")
    
    class Config:
        case_sensitive = True

settings = Settings()
