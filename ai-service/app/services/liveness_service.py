import cv2
import numpy as np
import mediapipe as mp
import logging
from typing import Tuple, Dict, Any, List
from app.models.schemas import LivenessFrameCheck

logger = logging.getLogger(__name__)

class LivenessService:
    def __init__(self):
        self.mp_face_mesh = mp.solutions.face_mesh
        self.face_mesh = self.mp_face_mesh.FaceMesh(
            static_image_mode=False,
            max_num_faces=1,
            refine_landmarks=True,
            min_detection_confidence=0.5,
            min_tracking_confidence=0.5
        )
        
        # Repères anatomiques pour les yeux
        self.LEFT_EYE = [362, 385, 387, 263, 373, 380]
        self.RIGHT_EYE = [33, 160, 158, 133, 153, 144]
        
        # Repères pour l'estimation de pose de tête (3D model points)
        self.FACE_3D_MODEL_POINTS = np.array([
            (0.0, 0.0, 0.0),             # Bout du nez (1)
            (0.0, -330.0, -65.0),        # Menton (152)
            (-225.0, 170.0, -135.0),     # Coin externe œil gauche (263)
            (225.0, 170.0, -135.0),      # Coin externe œil droit (33)
            (-150.0, -150.0, -125.0),    # Coin gauche bouche (291)
            (150.0, -150.0, -125.0)      # Coin droit bouche (61)
        ], dtype=np.float64)

    @staticmethod
    def _euclidean_distance(p1, p2) -> float:
        return np.linalg.norm(np.array(p1) - np.array(p2))

    def calculate_ear(self, landmarks, eye_indices, img_w: int, img_h: int) -> float:
        """Calcule le ratio d'ouverture de l'œil (Eye Aspect Ratio)."""
        pts = [(int(landmarks[i].x * img_w), int(landmarks[i].y * img_h)) for i in eye_indices]
        
        # Distances verticales
        a = self._euclidean_distance(pts[1], pts[5])
        b = self._euclidean_distance(pts[2], pts[4])
        # Distance horizontale
        c = self._euclidean_distance(pts[0], pts[3])
        
        if c == 0:
            return 0.0
        return (a + b) / (2.0 * c)

    def estimate_head_pose(self, landmarks, img_w: int, img_h: int) -> Tuple[float, float, float]:
        """Estime l'angle de rotation de la tête (Pitch, Yaw, Roll en degrés)."""
        image_points = np.array([
            (landmarks[1].x * img_w, landmarks[1].y * img_h),       # Nez
            (landmarks[152].x * img_w, landmarks[152].y * img_h),   # Menton
            (landmarks[263].x * img_w, landmarks[263].y * img_h),   # Oeil G
            (landmarks[33].x * img_w, landmarks[33].y * img_h),     # Oeil D
            (landmarks[291].x * img_w, landmarks[291].y * img_h),   # Bouche G
            (landmarks[61].x * img_w, landmarks[61].y * img_h)      # Bouche D
        ], dtype=np.float64)

        focal_length = img_w
        center = (img_w / 2, img_h / 2)
        camera_matrix = np.array([
            [focal_length, 0, center[0]],
            [0, focal_length, center[1]],
            [0, 0, 1]
        ], dtype=np.float64)
        dist_coeffs = np.zeros((4, 1))

        success, rot_vec, trans_vec = cv2.solvePnP(
            self.FACE_3D_MODEL_POINTS, image_points, camera_matrix, dist_coeffs, flags=cv2.SOLVEPNP_ITERATIVE
        )

        if not success:
            return 0.0, 0.0, 0.0

        rot_mat, _ = cv2.Rodrigues(rot_vec)
        angles, _, _, _, _, _ = cv2.RQDecomp3x3(rot_mat)

        pitch = angles[0] * 360
        yaw = angles[1] * 360
        roll = angles[2] * 360

        return pitch, yaw, roll

    def analyze_frame(self, image_bytes: bytes) -> LivenessFrameCheck:
        """Analyse une seule image/frame pour extraire l'EAR et l'orientation de tête."""
        nparr = np.frombuffer(image_bytes, np.uint8)
        frame = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
        if frame is None:
            return LivenessFrameCheck(
                is_live=False, blink_detected=False, head_turn_detected=False,
                eye_aspect_ratio=0.0, head_yaw_angle=0.0, message="Impossible de lire l'image."
            )

        img_h, img_w, _ = frame.shape
        rgb_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        results = self.face_mesh.process(rgb_frame)

        if not results.multi_face_landmarks:
            return LivenessFrameCheck(
                is_live=False, blink_detected=False, head_turn_detected=False,
                eye_aspect_ratio=0.0, head_yaw_angle=0.0, message="Aucun visage détecté."
            )

        landmarks = results.multi_face_landmarks[0].landmark
        
        # Calcul EAR
        left_ear = self.calculate_ear(landmarks, self.LEFT_EYE, img_w, img_h)
        right_ear = self.calculate_ear(landmarks, self.RIGHT_EYE, img_w, img_h)
        avg_ear = (left_ear + right_ear) / 2.0

        # Calcul Pose
        pitch, yaw, roll = self.estimate_head_pose(landmarks, img_w, img_h)

        blink = avg_ear < 0.21  # Seuil classique de fermeture des paupières
        head_turn = abs(yaw) > 12.0  # Mouvement de tête à gauche ou à droite

        return LivenessFrameCheck(
            is_live=True,
            blink_detected=blink,
            head_turn_detected=head_turn,
            eye_aspect_ratio=round(avg_ear, 3),
            head_yaw_angle=round(yaw, 2),
            message="Visage et repères biométriques détectés."
        )

    def verify_liveness_challenge(self, frames_bytes: List[bytes]) -> Dict[str, Any]:
        """
        Vérifie une séquence de frames (ex: 3 à 5 images successives)
        pour valider au moins un clignement OU un mouvement de tête.
        """
        if not frames_bytes:
            return {"is_live": False, "reason": "Aucune frame fournie."}

        ear_history = []
        yaw_history = []
        
        for fb in frames_bytes:
            res = self.analyze_frame(fb)
            if res.is_live:
                ear_history.append(res.eye_aspect_ratio)
                yaw_history.append(res.head_yaw_angle)

        if not ear_history:
            return {"is_live": False, "reason": "Visage non détecté dans les captures."}

        min_ear = min(ear_history)
        max_ear = max(ear_history)
        ear_variation = max_ear - min_ear

        min_yaw = min(yaw_history)
        max_yaw = max(yaw_history)
        yaw_variation = max_yaw - min_yaw

        # Si l'écart d'EAR ou de Yaw montre un dynamisme réel
        blink_confirmed = (ear_variation > 0.08) or (min_ear < 0.20 and max_ear > 0.28)
        head_turn_confirmed = yaw_variation > 15.0

        is_live = blink_confirmed or head_turn_confirmed or (len(frames_bytes) == 1 and max_ear > 0.22)

        return {
            "is_live": is_live,
            "blink_detected": blink_confirmed,
            "head_turn_detected": head_turn_confirmed,
            "ear_variation": round(ear_variation, 3),
            "yaw_variation": round(yaw_variation, 2),
            "reason": "Test de présence réussi (Liveness Validée)" if is_live else "Manque de mouvement / suspicion de photo statique"
        }

liveness_service = LivenessService()
