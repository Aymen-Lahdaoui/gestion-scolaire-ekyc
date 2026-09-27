from fastapi import APIRouter, UploadFile, File, Form, HTTPException, status
from typing import List, Optional
import logging
from app.services.ocr_service import OCRService
from app.services.face_service import FaceService
from app.services.liveness_service import liveness_service
from app.services.db_service import DatabaseService
from app.models.schemas import EKYCResponse, OCRExtractionResult

logger = logging.getLogger(__name__)
router = APIRouter(prefix="/ekyc", tags=["eKYC - Vérification d'Identité"])

@router.post("/verify", response_model=EKYCResponse)
async def verify_ekyc(
    cin_card: UploadFile = File(..., description="Image recto de la Carte d'Identité Nationale (CIN)"),
    selfie: UploadFile = File(..., description="Selfie direct capturé par la webcam"),
    etudiant_id: Optional[int] = Form(None, description="ID de l'étudiant à mettre à jour")
):
    """
    Pipeline eKYC complet :
    1. Extraction OCR des informations de la CIN (Nom, Prénom, Numéro CIN).
    2. Test de vivacité / Liveness sur le selfie direct.
    3. Comparaison biométrique ArcFace entre la photo de la CIN et le selfie.
    4. Décision automatique : VALIDE (> 80%), EN_REVISION (60-80%), REJETE (< 60%).
    5. Sauvegarde automatique du vecteur 512D si validé.
    """
    try:
        cin_bytes = await cin_card.read()
        selfie_bytes = await selfie.read()
        
        if not cin_bytes or not selfie_bytes:
            raise HTTPException(status_code=400, detail="Fichiers d'images invalides ou vides.")
            
        # 1. Extraction OCR
        ocr_res = OCRService.extract_cin_info(cin_bytes)
        
        # 2. Liveness Check
        liveness_res = liveness_service.analyze_frame(selfie_bytes)
        is_live = liveness_res.is_live
        
        # 3. Matching Biométrique (ArcFace)
        try:
            distance, similarity = FaceService.compare_faces(cin_bytes, selfie_bytes)
        except Exception as e:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail=f"Erreur lors de la détection faciale: {str(e)}"
            )
            
        # 4. Décision eKYC
        statut, message = FaceService.evaluate_ekyc_decision(similarity, is_live)
        
        # 5. Extraction du vecteur 512D pour stockage
        face_embedding = None
        if statut in ["VALIDE", "EN_REVISION"]:
            try:
                face_embedding = FaceService.extract_embedding(selfie_bytes)
                if etudiant_id and statut == "VALIDE":
                    DatabaseService.save_student_embedding(etudiant_id, face_embedding)
            except Exception as e:
                logger.warning(f"Impossible d'extraire le vecteur selfie final: {e}")
                
        return EKYCResponse(
            statut_kyc=statut,
            similarity_score=similarity,
            face_distance=distance,
            ocr_result=ocr_res,
            is_live=is_live,
            liveness_details={
                "blink_detected": liveness_res.blink_detected,
                "head_turn_detected": liveness_res.head_turn_detected,
                "eye_aspect_ratio": liveness_res.eye_aspect_ratio,
                "head_yaw_angle": liveness_res.head_yaw_angle
            },
            face_embedding=face_embedding,
            message=message
        )
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Erreur inattendue pipeline eKYC: {e}")
        raise HTTPException(status_code=500, detail=f"Erreur interne du serveur: {str(e)}")

@router.post("/ocr", response_model=OCRExtractionResult)
async def extract_cin_ocr(
    cin_card: UploadFile = File(..., description="Image de la CIN pour extraction OCR")
):
    """Extrait uniquement les données textuelles de la carte d'identité."""
    try:
        cin_bytes = await cin_card.read()
        return OCRService.extract_cin_info(cin_bytes)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
