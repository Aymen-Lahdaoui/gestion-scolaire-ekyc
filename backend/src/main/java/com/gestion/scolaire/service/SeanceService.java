package com.gestion.scolaire.service;

import com.gestion.scolaire.dto.seance.SeanceCreateDto;
import com.gestion.scolaire.dto.seance.SeanceDto;
import com.gestion.scolaire.dto.utilisateur.UtilisateurDto;
import com.gestion.scolaire.entity.Emargement;
import com.gestion.scolaire.entity.Inscription;
import com.gestion.scolaire.entity.Seance;
import com.gestion.scolaire.entity.Utilisateur;
import com.gestion.scolaire.enums.MethodeEmargement;
import com.gestion.scolaire.enums.Role;
import com.gestion.scolaire.enums.StatutEmargement;
import com.gestion.scolaire.enums.StatutSeance;
import com.gestion.scolaire.exception.ResourceNotFoundException;
import com.gestion.scolaire.repository.EmargementRepository;
import com.gestion.scolaire.repository.InscriptionRepository;
import com.gestion.scolaire.repository.SeanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeanceService {

    private final SeanceRepository seanceRepository;
    private final InscriptionRepository inscriptionRepository;
    private final EmargementRepository emargementRepository;
    private final MatiereService matiereService;
    private final UtilisateurService utilisateurService;

    @Transactional(readOnly = true)
    public List<SeanceDto> findAll() {
        return seanceRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public SeanceDto findById(Long id) {
        return toDto(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<SeanceDto> findByProfesseur(Long professeurId) {
        return seanceRepository.findByProfesseurId(professeurId)
                .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<SeanceDto> findByMatiere(Long matiereId) {
        return seanceRepository.findByMatiereId(matiereId)
                .stream().map(this::toDto).toList();
    }

    @Transactional
    public SeanceDto create(SeanceCreateDto dto) {
        validateTimes(dto);
        Seance seance = Seance.builder()
                .matiere(matiereService.getOrThrow(dto.matiereId()))
                .professeur(getProfesseur(dto.professeurId()))
                .salle(dto.salle())
                .heureDebut(dto.heureDebut())
                .heureFin(dto.heureFin())
                .statut(StatutSeance.PLANIFIEE)
                .build();
        return toDto(seanceRepository.save(seance));
    }

    @Transactional
    public SeanceDto update(Long id, SeanceCreateDto dto) {
        validateTimes(dto);
        Seance seance = getOrThrow(id);
        if (seance.getStatut() == StatutSeance.TERMINEE) {
            throw new IllegalStateException("Impossible de modifier une séance terminée");
        }
        seance.setMatiere(matiereService.getOrThrow(dto.matiereId()));
        seance.setProfesseur(getProfesseur(dto.professeurId()));
        seance.setSalle(dto.salle());
        seance.setHeureDebut(dto.heureDebut());
        seance.setHeureFin(dto.heureFin());
        return toDto(seanceRepository.save(seance));
    }

    @Transactional
    public void delete(Long id) {
        Seance seance = getOrThrow(id);
        seanceRepository.delete(seance);
        log.info("Deleted seance id={}", id);
    }

    /**
     * Transitions a PLANIFIEE session to EN_COURS.
     */
    @Transactional
    public SeanceDto lancerSeance(Long id) {
        Seance seance = getOrThrow(id);
        if (seance.getStatut() != StatutSeance.PLANIFIEE) {
            throw new IllegalStateException(
                    "La séance ne peut être lancée que depuis l'état PLANIFIEE (état actuel : "
                            + seance.getStatut() + ")");
        }
        seance.setStatut(StatutSeance.EN_COURS);
        log.info("Session {} started (EN_COURS)", id);
        return toDto(seanceRepository.save(seance));
    }

    /**
     * Transitions a session to TERMINEE and marks all non-signed students as ABSENT.
     */
    @Transactional
    public SeanceDto fermerSeance(Long id) {
        Seance seance = getOrThrow(id);
        if (seance.getStatut() == StatutSeance.TERMINEE) {
            throw new IllegalStateException("La séance est déjà terminée");
        }

        // Find all enrolled students for this subject
        List<Inscription> inscriptions =
                inscriptionRepository.findByMatiereId(seance.getMatiere().getId());

        for (Inscription inscription : inscriptions) {
            Long etudiantId = inscription.getEtudiant().getId();
            boolean alreadyMarked =
                    emargementRepository.existsBySeanceIdAndEtudiantId(id, etudiantId);
            if (!alreadyMarked) {
                Emargement emargement = Emargement.builder()
                        .seance(seance)
                        .etudiant(inscription.getEtudiant())
                        .statut(StatutEmargement.ABSENT)
                        .methode(MethodeEmargement.MANUEL)
                        .build();
                emargementRepository.save(emargement);
            }
        }

        seance.setStatut(StatutSeance.TERMINEE);
        log.info("Session {} closed (TERMINEE), absences recorded", id);
        return toDto(seanceRepository.save(seance));
    }

    /**
     * Returns the list of enrolled students for the session's subject.
     */
    @Transactional(readOnly = true)
    public List<UtilisateurDto> getStudentsForSeance(Long seanceId) {
        Seance seance = getOrThrow(seanceId);
        return inscriptionRepository.findByMatiereId(seance.getMatiere().getId())
                .stream()
                .map(i -> utilisateurService.toDto(i.getEtudiant()))
                .toList();
    }

    public Seance getOrThrow(Long id) {
        return seanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Séance", "id", id));
    }

    private Utilisateur getProfesseur(Long id) {
        Utilisateur u = utilisateurService.getOrThrow(id);
        if (u.getRole() != Role.ENSEIGNANT && u.getRole() != Role.ADMINISTRATEUR) {
            throw new IllegalArgumentException("L'utilisateur doit être un enseignant ou un administrateur");
        }
        return u;
    }

    private void validateTimes(SeanceCreateDto dto) {
        if (!dto.heureFin().isAfter(dto.heureDebut())) {
            throw new IllegalArgumentException(
                    "L'heure de fin doit être postérieure à l'heure de début");
        }
    }

    public SeanceDto toDto(Seance s) {
        return new SeanceDto(
                s.getId(),
                s.getMatiere().getId(),
                s.getMatiere().getNom(),
                s.getMatiere().getCode(),
                s.getProfesseur().getId(),
                s.getProfesseur().getNom(),
                s.getProfesseur().getPrenom(),
                s.getSalle(),
                s.getHeureDebut(),
                s.getHeureFin(),
                s.getStatut()
        );
    }
}
