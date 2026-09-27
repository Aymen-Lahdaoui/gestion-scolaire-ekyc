from typing import List, Optional, Dict, Any
from pydantic import BaseModel, Field

class LivenessFrameCheck(BaseModel):
    is_live: bool
    blink_detected: bool
    head_turn_detected: bool
    eye_aspect_ratio: float
    head_yaw_angle: float
    message: str

class OCRExtractionResult(BaseModel):
    nom: Optional[str] = None
    prenom: Optional[str] = None
    cin_numero: Optional[str] = None
    raw_text: str
    confidence_score: float

class EKYCResponse(BaseModel):
    statut_kyc: str  # "VALIDE", "EN_REVISION", "REJETE"
    similarity_score: float = Field(..., description="Pourcentage de similarité entre 0 et 100%")
    face_distance: float
    ocr_result: OCRExtractionResult
    is_live: bool
    liveness_details: Dict[str, Any]
    face_embedding: Optional[List[float]] = None
    message: str

class RecognizeFaceResponse(BaseModel):
    status: str  # "Recognized", "Unrecognized"
    student_id: Optional[int] = None
    confidence: Optional[float] = None
    distance: Optional[float] = None
    message: Optional[str] = None

class EmbeddingResponse(BaseModel):
    embedding: List[float]
    dimension: int = 512
