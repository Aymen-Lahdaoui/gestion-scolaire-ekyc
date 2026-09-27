from fastapi import APIRouter, UploadFile, File, HTTPException
from typing import List
import logging
from app.services.liveness_service import liveness_service
from app.models.schemas import LivenessFrameCheck

logger = logging.getLogger(__name__)
router = APIRouter(prefix="/liveness", tags=["Liveness Detection (Anti-Spoofing)"])

@router.post("/check-frame", response_model=LivenessFrameCheck)
async def check_single_frame(
    frame: UploadFile = File(..., description="Frame unique envoyée par le frontend")
):
    """Analyse en direct une frame vidéo pour renvoyer le ratio EAR et l'angle de pose."""
    try:
        frame_bytes = await frame.read()
        return liveness_service.analyze_frame(frame_bytes)
    except Exception as e:
        logger.error(f"Erreur analyse frame liveness: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/verify-challenge")
async def verify_challenge(
    frames: List[UploadFile] = File(..., description="Séquence de 3 à 5 captures consécutives")
):
    """
    Vérifie le challenge de vivacité active (clignements et rotations de tête)
    sur une séquence d'images capturées en direct.
    """
    try:
        frames_bytes = [await f.read() for f in frames]
        result = liveness_service.verify_liveness_challenge(frames_bytes)
        return result
    except Exception as e:
        logger.error(f"Erreur challenge liveness: {e}")
        raise HTTPException(status_code=500, detail=str(e))
