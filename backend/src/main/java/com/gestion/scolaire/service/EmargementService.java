package com.gestion.scolaire.service;

import com.gestion.scolaire.dto.emargement.EmargementCreateDto;
import com.gestion.scolaire.dto.emargement.EmargementDto;
import com.gestion.scolaire.dto.emargement.EmargementUpdateDto;
import com.gestion.scolaire.dto.emargement.SeanceEmargementReport;
import com.gestion.scolaire.entity.Emargement;
import com.gestion.scolaire.entity.Inscription;
import com.gestion.scolaire.entity.Seance;
import com.gestion.scolaire.entity.Utilisateur;
import com.gestion.scolaire.enums.StatutEmargement;
import com.gestion.scolaire.enums.StatutSeance;
import com.gestion.scolaire.exception.DuplicateResourceException;
import com.gestion.scolaire.exception.ResourceNotFoundException;
import com.gestion.scolaire.repository.EmargementRepository;
import com.gestion.scolaire.repository.InscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmargementService {

    private final EmargementRepository emargementRepository;
    private final SeanceService seanceService;
    private final UtilisateurService utilisateurService;
    private final InscriptionRepository inscriptionRepository;

    @Transactional(readOnly = true)
    public List<EmargementDto> findBySeance(Long seanceId) {
        return emargementRepository.findBySeanceIdWithDetails(seanceId)
                .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<EmargementDto> findByEtudiant(Long etudiantId) {
        return emargementRepository.findByEtudiantId(etudiantId)
                .stream().map(this::toDto).toList();
    }

    /**
     * Marks a student present (or with RETARD) for a session.
     * Guards against duplicate entries.
     */
    @Transactional
    public EmargementDto markPresent(EmargementCreateDto dto) {
        Seance seance = seanceService.getOrThrow(dto.seanceId());

        if (seance.getStatut() != StatutSeance.EN_COURS) {
            throw new IllegalStateException(
                    "L'émargement n'est possible que pour une séance EN_COURS");
        }

        Utilisateur etudiant = utilisateurService.getOrThrow(dto.etudiantId());

        // Check if student is enrolled in this subject
        if (!inscriptionRepository.existsByEtudiantIdAndMatiereId(
                dto.etudiantId(), seance.getMatiere().getId())) {
            throw new IllegalArgumentException(
                    "L'étudiant n'est pas inscrit à cette matière");
        }

        // Guard against duplicate present marking
        if (emargementRepository.existsBySeanceIdAndEtudiantId(dto.seanceId(), dto.etudiantId())) {
            Emargement existing = emargementRepository
                    .findBySeanceIdAndEtudiantId(dto.seanceId(), dto.etudiantId())
                    .orElseThrow();
            if (existing.getStatut() == StatutEmargement.PRESENT) {
                throw new DuplicateResourceException(
                        "L'étudiant est déjà marqué PRESENT pour cette séance");
            }
            // Update existing record (e.g., was ABSENT, now PRESENT after facial recognition)
            existing.setStatut(dto.statut());
            existing.setMethode(dto.methode());
            log.info("Updated emargement for etudiant={} seance={}", dto.etudiantId(), dto.seanceId());
            return toDto(emargementRepository.save(existing));
        }

        Emargement emargement = Emargement.builder()
                .seance(seance)
                .etudiant(etudiant)
                .statut(dto.statut())
                .methode(dto.methode())
                .build();

        log.info("Marked etudiant={} as {} for seance={}", dto.etudiantId(), dto.statut(), dto.seanceId());
        return toDto(emargementRepository.save(emargement));
    }

    /**
     * Manually updates an existing attendance record.
     */
    @Transactional
    public EmargementDto updateManually(Long emargementId, EmargementUpdateDto dto) {
        Emargement emargement = emargementRepository.findById(emargementId)
                .orElseThrow(() -> new ResourceNotFoundException("Émargement", "id", emargementId));
        emargement.setStatut(dto.statut());
        emargement.setMethode(dto.methode());
        log.info("Manual update: emargement={} -> statut={}", emargementId, dto.statut());
        return toDto(emargementRepository.save(emargement));
    }

    /**
     * Generates a full attendance report for a session.
     */
    @Transactional(readOnly = true)
    public SeanceEmargementReport getReport(Long seanceId) {
        Seance seance = seanceService.getOrThrow(seanceId);
        List<Emargement> emargements = emargementRepository.findBySeanceIdWithDetails(seanceId);
        List<Inscription> inscriptions = inscriptionRepository
                .findByMatiereId(seance.getMatiere().getId());

        List<EmargementDto> dtos = emargements.stream().map(this::toDto).toList();

        int totalPresents = (int) emargements.stream()
                .filter(e -> e.getStatut() == StatutEmargement.PRESENT).count();
        int totalAbsents = (int) emargements.stream()
                .filter(e -> e.getStatut() == StatutEmargement.ABSENT).count();
        int totalRetards = (int) emargements.stream()
                .filter(e -> e.getStatut() == StatutEmargement.RETARD).count();
        int totalInscrits = inscriptions.size();
        double tauxPresence = totalInscrits > 0
                ? (double) totalPresents / totalInscrits * 100.0 : 0.0;

        return new SeanceEmargementReport(
                seanceId,
                seance.getMatiere().getNom(),
                seance.getMatiere().getCode(),
                seance.getSalle(),
                totalInscrits,
                totalPresents,
                totalAbsents,
                totalRetards,
                Math.round(tauxPresence * 100.0) / 100.0,
                dtos
        );
    }

    public EmargementDto toDto(Emargement e) {
        return new EmargementDto(
                e.getId(),
                e.getSeance().getId(),
                e.getEtudiant().getId(),
                e.getEtudiant().getNom(),
                e.getEtudiant().getPrenom(),
                e.getStatut(),
                e.getMethode(),
                e.getHorodatage()
        );
    }
}
