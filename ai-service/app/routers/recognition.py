from fastapi import APIRouter, UploadFile, File, Query, HTTPException, status
import logging
from app.services.face_service import FaceService
from app.services.db_service import DatabaseService
from app.models.schemas import RecognizeFaceResponse, EmbeddingResponse
from app.config import settings

logger = logging.getLogger(__name__)
router = APIRouter(tags=["Reconnaissance Faciale & Émargement"])

@router.post("/recognize-face", response_model=RecognizeFaceResponse)
async def recognize_face(
    image: UploadFile = File(..., description="Frame vidéo capturée par la webcam"),
    threshold: float = Query(
        default=0.40,
        ge=0.1,
        le=1.0,
        description="Seuil maximal de distance cosinus (défaut 0.40)"
    )
):
    """
    Endpoint d'émargement en direct (Flux Webcam) :
    1. Reçoit une capture d'image JPEG.
    2. Détecte le visage et extrait l'empreinte ArcFace (vecteur 512D).
    3. Effectue la recherche vectorielle (<=>) dans PostgreSQL (pgvector).
    4. Retourne l'identifiant de l'étudiant avec le score de confiance.
    """
    try:
        image_bytes = await image.read()
        if not image_bytes:
            raise HTTPException(status_code=400, detail="Fichier image vide.")
            
        # 1. Extraction du vecteur
        try:
            query_embedding = FaceService.extract_embedding(image_bytes)
        except Exception as e:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Aucun visage détecté sur la capture."
            )
            
        # 2. Recherche vectorielle dans PostgreSQL
        etudiant_id, distance = DatabaseService.find_nearest_student(query_embedding, threshold=threshold)
        
        if etudiant_id is not None and distance is not None:
            # Calcul du score de confiance
            confidence = max(0.0, min(100.0, (1.0 - (distance / threshold)) * 100.0))
            if distance <= 0.20:
                confidence = 98.5
            elif distance <= 0.30:
                confidence = 94.0
            elif distance <= 0.38:
                confidence = 88.0
                
            return RecognizeFaceResponse(
                status="Recognized",
                student_id=etudiant_id,
                confidence=round(confidence, 1),
                distance=round(distance, 4),
                message=f"Étudiant #{etudiant_id} identifié avec succès."
            )
        else:
            return RecognizeFaceResponse(
                status="Unrecognized",
                student_id=None,
                confidence=None,
                distance=round(distance, 4) if distance is not None else None,
                message="Visage non reconnu ou non inscrit dans la base de données."
            )
            
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Erreur lors de la reconnaissance faciale: {e}")
        raise HTTPException(status_code=500, detail=f"Erreur serveur: {str(e)}")

@router.post("/extract-embedding", response_model=EmbeddingResponse)
async def extract_face_embedding(
    image: UploadFile = File(..., description="Image pour extraction de vecteur")
):
    """Extrait directement le vecteur d'empreinte faciale de 512 dimensions."""
    try:
        image_bytes = await image.read()
        embedding = FaceService.extract_embedding(image_bytes)
        return EmbeddingResponse(embedding=embedding, dimension=len(embedding))
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))
