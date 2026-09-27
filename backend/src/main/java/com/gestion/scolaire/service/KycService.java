package com.gestion.scolaire.service;

import com.gestion.scolaire.dto.utilisateur.KycStatusDto;
import com.gestion.scolaire.dto.utilisateur.UtilisateurDto;
import com.gestion.scolaire.entity.BiometrieEtudiant;
import com.gestion.scolaire.entity.Utilisateur;
import com.gestion.scolaire.enums.Role;
import com.gestion.scolaire.enums.StatutKyc;
import com.gestion.scolaire.exception.ResourceNotFoundException;
import com.gestion.scolaire.repository.BiometrieEtudiantRepository;
import com.gestion.scolaire.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class KycService {

    private final UtilisateurRepository utilisateurRepository;
    private final BiometrieEtudiantRepository biometrieEtudiantRepository;
    private final UtilisateurService utilisateurService;

    @Transactional(readOnly = true)
    public List<UtilisateurDto> getPendingKyc() {
        return utilisateurRepository.findByStatutKyc(StatutKyc.EN_ATTENTE)
                .stream()
                .map(utilisateurService::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UtilisateurDto> getByKycStatus(StatutKyc statut) {
        return utilisateurRepository.findByStatutKyc(statut)
                .stream()
                .map(utilisateurService::toDto)
                .toList();
    }

    @Transactional
    public KycStatusDto validateKyc(Long utilisateurId) {
        Utilisateur utilisateur = getOrThrow(utilisateurId);
        utilisateur.setStatutKyc(StatutKyc.VALIDE);
        utilisateurRepository.save(utilisateur);
        log.info("KYC validated for user id={}", utilisateurId);
        return new KycStatusDto(utilisateurId, StatutKyc.VALIDE, null);
    }

    @Transactional
    public KycStatusDto rejectKyc(Long utilisateurId, String motifRejet) {
        Utilisateur utilisateur = getOrThrow(utilisateurId);
        utilisateur.setStatutKyc(StatutKyc.REJETE);
        utilisateurRepository.save(utilisateur);
        log.info("KYC rejected for user id={}, motif={}", utilisateurId, motifRejet);
        return new KycStatusDto(utilisateurId, StatutKyc.REJETE, motifRejet);
    }

    @Transactional
    public KycStatusDto setUnderRevision(Long utilisateurId) {
        Utilisateur utilisateur = getOrThrow(utilisateurId);
        utilisateur.setStatutKyc(StatutKyc.EN_REVISION);
        utilisateurRepository.save(utilisateur);
        log.info("KYC set to EN_REVISION for user id={}", utilisateurId);
        return new KycStatusDto(utilisateurId, StatutKyc.EN_REVISION, null);
    }

    /**
     * Stores or updates the biometric face embedding vector for a student.
     *
     * @param etudiantId     the student's ID
     * @param faceEmbedding  float array of 512 dimensions
     */
    @Transactional
    public void storeBiometricVector(Long etudiantId, float[] faceEmbedding) {
        if (faceEmbedding == null || faceEmbedding.length != 512) {
            throw new IllegalArgumentException(
                    "Le vecteur biométrique doit avoir exactement 512 dimensions");
        }

        Utilisateur etudiant = getOrThrow(etudiantId);
        if (etudiant.getRole() != Role.ETUDIANT) {
            throw new IllegalArgumentException(
                    "Les données biométriques ne peuvent être enregistrées que pour les étudiants");
        }

        Optional<BiometrieEtudiant> existing = biometrieEtudiantRepository.findByEtudiantId(etudiantId);
        BiometrieEtudiant biometrie;

        if (existing.isPresent()) {
            biometrie = existing.get();
            biometrie.setFaceEmbedding(faceEmbedding);
            biometrie.setDateVerification(LocalDateTime.now());
        } else {
            biometrie = BiometrieEtudiant.builder()
                    .etudiant(etudiant)
                    .faceEmbedding(faceEmbedding)
                    .dateVerification(LocalDateTime.now())
                    .build();
        }

        biometrieEtudiantRepository.save(biometrie);
        log.info("Biometric vector stored/updated for etudiant id={}", etudiantId);
    }

    /**
     * Performs a nearest-neighbor search using pgvector cosine distance.
     *
     * @param queryEmbedding float[512] query vector
     * @param threshold      maximum cosine distance (0.0 = identical, 2.0 = opposite)
     * @return the matched student's ID, or empty if none found within threshold
     */
    @Transactional(readOnly = true)
    public Optional<Long> findMatchingStudent(float[] queryEmbedding, double threshold) {
        String vectorLiteral = toVectorLiteral(queryEmbedding);
        List<Object[]> results = biometrieEtudiantRepository
                .findEmbeddingsWithinThreshold(vectorLiteral, threshold);

        if (results.isEmpty()) {
            return Optional.empty();
        }

        Object[] best = results.get(0);
        Long etudiantId = ((Number) best[0]).longValue();
        double distance = ((Number) best[1]).doubleValue();
        log.debug("Facial match found: etudiantId={}, distance={}", etudiantId, distance);
        return Optional.of(etudiantId);
    }

    /**
     * Converts a float[] into the pgvector literal format: "[0.1,0.2,...,0.n]"
     */
    public String toVectorLiteral(float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            sb.append(embedding[i]);
            if (i < embedding.length - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private Utilisateur getOrThrow(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", id));
    }
}
