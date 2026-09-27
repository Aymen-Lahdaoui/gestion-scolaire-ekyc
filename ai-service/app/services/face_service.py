import os
import cv2
import numpy as np
from deepface import DeepFace
import logging
from typing import List, Optional, Tuple, Dict, Any
from app.config import settings

logger = logging.getLogger(__name__)

class FaceService:
    @staticmethod
    def bytes_to_numpy(image_bytes: bytes) -> np.ndarray:
        nparr = np.frombuffer(image_bytes, np.uint8)
        img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
        if img is None:
            raise ValueError("Impossible de décoder l'image fournie.")
        return img

    @classmethod
    def extract_embedding(cls, image_bytes: bytes) -> List[float]:
        """
        Extrait le vecteur d'empreinte faciale de 512 dimensions via le modèle ArcFace.
        """
        try:
            img = cls.bytes_to_numpy(image_bytes)
            
            # DeepFace represent avec ArcFace
            embeddings = DeepFace.represent(
                img_path=img,
                model_name=settings.MODEL_NAME,
                detector_backend=settings.DETECTOR_BACKEND,
                enforce_detection=True,
                align=True
            )
            
            if not embeddings:
                raise ValueError("Aucun visage détecté.")
                
            # Récupère le premier visage détecté
            vector = embeddings[0]["embedding"]
            logger.info(f"Vecteur facial extrait avec succès (taille: {len(vector)})")
            return vector
        except Exception as e:
            logger.error(f"Erreur extraction embedding: {e}")
            raise

    @classmethod
    def compare_faces(cls, img1_bytes: bytes, img2_bytes: bytes) -> Tuple[float, float]:
        """
        Compare deux visages (ex: Photo CIN vs Selfie direct).
        Retourne (distance_cosinus, score_similarite_pourcentage).
        """
        try:
            img1 = cls.bytes_to_numpy(img1_bytes)
            img2 = cls.bytes_to_numpy(img2_bytes)
            
            result = DeepFace.verify(
                img1_path=img1,
                img2_path=img2,
                model_name=settings.MODEL_NAME,
                detector_backend=settings.DETECTOR_BACKEND,
                distance_metric=settings.SIMILARITY_METRIC,
                enforce_detection=True,
                align=True
            )
            
            distance = float(result.get("distance", 1.0))
            
            # Formule de conversion Distance -> Pourcentage de similarité
            # Distance ArcFace typique: 0.0 (parfait) à 0.40 (seuil match 80%) et 0.60 (seuil révision 60%)
            if distance <= 0.0:
                similarity = 100.0
            else:
                # Modèle linéaire par morceaux pour une calibration précise
                if distance <= 0.40:
                    similarity = 100.0 - (distance / 0.40) * 20.0  # 100% -> 80%
                elif distance <= 0.60:
                    similarity = 80.0 - ((distance - 0.40) / 0.20) * 20.0  # 80% -> 60%
                else:
                    similarity = max(0.0, 60.0 - ((distance - 0.60) / 0.40) * 60.0)  # 60% -> 0%
                    
            similarity = round(similarity, 2)
            distance = round(distance, 4)
            
            logger.info(f"Comparaison faciale: Distance={distance}, Similarité={similarity}%")
            return distance, similarity
        except Exception as e:
            logger.error(f"Erreur comparaison faciale: {e}")
            raise

    @classmethod
    def evaluate_ekyc_decision(cls, similarity_score: float, is_live: bool) -> Tuple[str, str]:
        """
        Applique les règles métier du Cahier des Charges:
        - Score > 80% + Liveness OK : VALIDE
        - Score entre 60% et 80% OU doute liveness : EN_REVISION
        - Score < 60% : REJETE
        """
        if not is_live:
            return "REJETE", "Échec du test de présence réelle (Liveness)."

        if similarity_score >= settings.EKYC_AUTO_VALIDATE_SCORE:
            return "VALIDE", "Profil validé automatiquement avec succès (> 80%)."
        elif similarity_score >= settings.EKYC_MANUAL_REVIEW_SCORE:
            return "EN_REVISION", "Score modéré (60%-80%). Profil transféré en révision manuelle pour l'Administrateur."
        else:
            return "REJETE", "Similarité insuffisante (< 60%). Profil rejeté automatiquement."
