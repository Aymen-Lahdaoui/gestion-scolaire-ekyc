import re
import cv2
import numpy as np
import pytesseract
from PIL import Image
import logging
from typing import Tuple, Optional
from app.config import settings
from app.models.schemas import OCRExtractionResult

logger = logging.getLogger(__name__)

# Configurer le chemin tesseract si spécifié
if settings.TESSERACT_CMD:
    pytesseract.pytesseract.tesseract_cmd = settings.TESSERACT_CMD

class OCRService:
    @staticmethod
    def preprocess_for_ocr(image_bytes: bytes) -> np.ndarray:
        """Convertit l'image en niveaux de gris et applique un seuillage adaptatif pour optimiser l'OCR."""
        nparr = np.frombuffer(image_bytes, np.uint8)
        img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
        if img is None:
            raise ValueError("Impossible de décoder l'image de la pièce d'identité.")
        
        # Redimensionnement si trop petit
        h, w = img.shape[:2]
        if w < 1000:
            scaling = 1000.0 / w
            img = cv2.resize(img, (int(w * scaling), int(h * scaling)), interpolation=cv2.INTER_CUBIC)
            
        gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
        
        # Réduction du bruit + Rehaussement de contraste
        denoised = cv2.fastNlMeansDenoising(gray, h=10)
        clahe = cv2.createCLAHE(clipLimit=2.0, tileGridSize=(8, 8))
        enhanced = clahe.apply(denoised)
        
        return enhanced

    @classmethod
    def extract_cin_info(cls, image_bytes: bytes) -> OCRExtractionResult:
        """
        Extrait le numéro de CIN, Nom et Prénom depuis l'image de la carte d'identité.
        Supporte les formats standards de cartes d'identité (CIN marocaine / maghrébine / française).
        """
        try:
            processed_img = cls.preprocess_for_ocr(image_bytes)
            
            # OCR en français et arabe/latin
            custom_config = r'--oem 3 --psm 6'
            raw_text = pytesseract.image_to_string(processed_img, config=custom_config)
            
            logger.info(f"Texte brut extrait par OCR :\n{raw_text}")
            
            # Extraction par regex
            cin_numero = cls._extract_cin_number(raw_text)
            nom, prenom = cls._extract_names(raw_text)
            
            # Calcul d'un score de confiance basique basé sur la détection des champs
            confidence = 30.0
            if cin_numero:
                confidence += 40.0
            if nom:
                confidence += 15.0
            if prenom:
                confidence += 15.0
                
            return OCRExtractionResult(
                nom=nom,
                prenom=prenom,
                cin_numero=cin_numero,
                raw_text=raw_text.strip(),
                confidence_score=min(100.0, confidence)
            )
        except Exception as e:
            logger.error(f"Erreur lors de l'OCR de la CIN: {e}")
            return OCRExtractionResult(
                nom=None,
                prenom=None,
                cin_numero=None,
                raw_text="",
                confidence_score=0.0
            )

    @staticmethod
    def _extract_cin_number(text: str) -> Optional[str]:
        """Détecte les numéros de CIN (ex: AB123456, K54321, CD987654, 12345678)."""
        # Pattern standard CIN : 1 ou 2 lettres suivies de 5 à 7 chiffres
        patterns = [
            r'\b[A-Z]{1,2}\s?[0-9]{5,7}\b',
            r'CIN\s*[:\.]?\s*([A-Z0-9\s]+)',
            r'N°\s*[:\.]?\s*([A-Z0-9\s]+)',
            r'\b[0-9]{8}\b'
        ]
        
        for pat in patterns:
            match = re.search(pat, text, re.IGNORECASE)
            if match:
                res = match.group(0).replace(" ", "").upper()
                # Nettoyage des préfixes
                res = re.sub(r'^(CIN|N°|NO|ID)[:\.]?', '', res)
                if len(res) >= 5:
                    return res
        return None

    @staticmethod
    def _extract_names(text: str) -> Tuple[Optional[str], Optional[str]]:
        """Tente d'extraire le Nom et le Prénom depuis les lignes typiques de la carte."""
        lines = [l.strip() for l in text.split('\n') if len(l.strip()) > 2]
        nom = None
        prenom = None
        
        for i, line in enumerate(lines):
            upper_line = line.upper()
            if "NOM" in upper_line or "SURNAME" in upper_line:
                # La valeur est sur la même ligne ou la suivante
                parts = re.split(r'NOM|SURNAME', line, flags=re.IGNORECASE)
                if len(parts) > 1 and len(parts[1].strip()) > 1:
                    nom = re.sub(r'[^A-Za-zÀ-ÿ\s-]', '', parts[1]).strip()
                elif i + 1 < len(lines):
                    nom = re.sub(r'[^A-Za-zÀ-ÿ\s-]', '', lines[i+1]).strip()
                    
            if "PRENOM" in upper_line or "GIVEN" in upper_line or "FORENAME" in upper_line:
                parts = re.split(r'PRENOM|GIVEN|FORENAME', line, flags=re.IGNORECASE)
                if len(parts) > 1 and len(parts[1].strip()) > 1:
                    prenom = re.sub(r'[^A-Za-zÀ-ÿ\s-]', '', parts[1]).strip()
                elif i + 1 < len(lines):
                    prenom = re.sub(r'[^A-Za-zÀ-ÿ\s-]', '', lines[i+1]).strip()
                    
        return nom, prenom
