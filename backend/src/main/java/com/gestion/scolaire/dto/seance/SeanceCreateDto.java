package com.gestion.scolaire.dto.seance;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record SeanceCreateDto(
        @NotNull(message = "L'identifiant de la matière est obligatoire")
        Long matiereId,

        @NotNull(message = "L'identifiant du professeur est obligatoire")
        Long professeurId,

        String salle,

        @NotNull(message = "L'heure de début est obligatoire")
        LocalDateTime heureDebut,

        @NotNull(message = "L'heure de fin est obligatoire")
        LocalDateTime heureFin
) {}
