package com.gestion.scolaire.service;

import com.gestion.scolaire.repository.EmargementRepository;
import com.gestion.scolaire.repository.InscriptionRepository;
import com.gestion.scolaire.repository.SeanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatistiquesService {

    private final EmargementRepository emargementRepository;
    private final InscriptionRepository inscriptionRepository;
    private final SeanceRepository seanceRepository;
    private final MatiereService matiereService;

    /**
     * Computes the attendance rate (%) for a student in a given subject.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> tauxPresenceEtudiantParMatiere(Long etudiantId, Long matiereId) {
        long totalSeances = seanceRepository.findByMatiereId(matiereId).size();
        long presences = emargementRepository.countPresencesByEtudiantAndMatiere(etudiantId, matiereId);

        double taux = totalSeances > 0 ? (double) presences / totalSeances * 100.0 : 0.0;

        Map<String, Object> result = new HashMap<>();
        result.put("etudiantId", etudiantId);
        result.put("matiereId", matiereId);
        result.put("totalSeances", totalSeances);
        result.put("totalPresences", presences);
        result.put("tauxPresence", Math.round(taux * 100.0) / 100.0);
        return result;
    }

    /**
     * Computes the global attendance rate (%) for a given subject across all students.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> tauxPresenceParMatiere(Long matiereId) {
        long totalEmargements = emargementRepository.countTotalByMatiere(matiereId);
        long totalPresences = emargementRepository.countPresencesByMatiere(matiereId);

        double taux = totalEmargements > 0
                ? (double) totalPresences / totalEmargements * 100.0 : 0.0;

        Map<String, Object> result = new HashMap<>();
        result.put("matiereId", matiereId);
        result.put("totalEmargements", totalEmargements);
        result.put("totalPresences", totalPresences);
        result.put("tauxPresence", Math.round(taux * 100.0) / 100.0);
        return result;
    }

    /**
     * Computes attendance summary for all subjects.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> tauxPresenceToutesLesMatiere() {
        return matiereService.findAll()
                .stream()
                .map(m -> tauxPresenceParMatiere(m.id()))
                .toList();
    }

    /**
     * Computes overall stats across the entire platform.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> statistiquesGlobales() {
        long totalEmargements = emargementRepository.count();
        long totalPresences = emargementRepository.findAll()
                .stream()
                .filter(e -> e.getStatut().name().equals("PRESENT"))
                .count();
        long totalSeances = seanceRepository.count();
        long totalInscriptions = inscriptionRepository.count();

        double tauxGlobal = totalEmargements > 0
                ? (double) totalPresences / totalEmargements * 100.0 : 0.0;

        Map<String, Object> result = new HashMap<>();
        result.put("totalSeances", totalSeances);
        result.put("totalInscriptions", totalInscriptions);
        result.put("totalEmargements", totalEmargements);
        result.put("totalPresences", totalPresences);
        result.put("tauxPresenceGlobal", Math.round(tauxGlobal * 100.0) / 100.0);
        return result;
    }
}
