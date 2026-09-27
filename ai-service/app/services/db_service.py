import logging
from typing import Optional, Tuple
import psycopg2
from pgvector.psycopg2 import register_vector
import numpy as np
from app.config import settings

logger = logging.getLogger(__name__)

class DatabaseService:
    @staticmethod
    def get_connection():
        conn = psycopg2.connect(
            host=settings.DB_HOST,
            port=settings.DB_PORT,
            dbname=settings.DB_NAME,
            user=settings.DB_USER,
            password=settings.DB_PASSWORD
        )
        register_vector(conn)
        return conn

    @classmethod
    def find_nearest_student(cls, embedding: list[float], threshold: float = 0.40) -> Tuple[Optional[int], Optional[float]]:
        """
        Recherche vectorielle du plus proche voisin dans biometrie_etudiants.
        Utilise la distance cosinus (<=>) de pgvector.
        Retourne (etudiant_id, distance) si distance <= threshold, sinon (None, distance).
        """
        try:
            with cls.get_connection() as conn:
                with conn.cursor() as cur:
                    query = """
                        SELECT etudiant_id, (face_embedding <=> %s::vector) AS distance
                        FROM biometrie_etudiants
                        ORDER BY distance ASC
                        LIMIT 1;
                    """
                    emb_array = np.array(embedding, dtype=np.float32)
                    cur.execute(query, (emb_array,))
                    row = cur.fetchone()
                    
                    if row:
                        etudiant_id, distance = row[0], float(row[1])
                        logger.info(f"Recherche pgvector: etudiant_id={etudiant_id}, distance={distance:.4f} (seuil={threshold})")
                        if distance <= threshold:
                            return etudiant_id, distance
                        return None, distance
                    return None, None
        except Exception as e:
            logger.error(f"Erreur lors de la recherche vectorielle pgvector: {e}")
            return None, None

    @classmethod
    def save_student_embedding(cls, etudiant_id: int, embedding: list[float]) -> bool:
        """
        Enregistre ou met à jour le vecteur d'empreinte faciale (512 dimensions) d'un étudiant.
        """
        try:
            with cls.get_connection() as conn:
                with conn.cursor() as cur:
                    emb_array = np.array(embedding, dtype=np.float32)
                    query = """
                        INSERT INTO biometrie_etudiants (etudiant_id, face_embedding, date_verification)
                        VALUES (%s, %s::vector, NOW())
                        ON CONFLICT (etudiant_id) 
                        DO UPDATE SET face_embedding = EXCLUDED.face_embedding, date_verification = NOW();
                    """
                    cur.execute(query, (etudiant_id, emb_array))
                    conn.commit()
                    return True
        except Exception as e:
            logger.error(f"Erreur lors de l'enregistrement de l'embedding: {e}")
            return False
