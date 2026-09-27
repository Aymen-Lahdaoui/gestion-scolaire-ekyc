package com.gestion.scolaire.dto.emargement;

import java.util.List;

public record SeanceEmargementReport(
        Long seanceId,
        String matiereNom,
        String matiereCode,
        String salle,
        int totalInscrits,
        int totalPresents,
        int totalAbsents,
        int totalRetards,
        double tauxPresence,
        List<EmargementDto> emargements
) {}
