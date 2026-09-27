package com.gestion.scolaire.dto.seance;

import com.gestion.scolaire.enums.StatutSeance;

import java.time.LocalDateTime;

public record SeanceDto(
        Long id,
        Long matiereId,
        String matiereNom,
        String matiereCode,
        Long professeurId,
        String professeurNom,
        String professeurPrenom,
        String salle,
        LocalDateTime heureDebut,
        LocalDateTime heureFin,
        StatutSeance statut
) {}
