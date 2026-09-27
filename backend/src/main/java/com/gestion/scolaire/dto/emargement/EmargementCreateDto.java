package com.gestion.scolaire.dto.emargement;

import com.gestion.scolaire.enums.MethodeEmargement;
import com.gestion.scolaire.enums.StatutEmargement;
import jakarta.validation.constraints.NotNull;

public record EmargementCreateDto(
        @NotNull(message = "L'identifiant de la séance est obligatoire")
        Long seanceId,

        @NotNull(message = "L'identifiant de l'étudiant est obligatoire")
        Long etudiantId,

        @NotNull(message = "Le statut est obligatoire")
        StatutEmargement statut,

        @NotNull(message = "La méthode est obligatoire")
        MethodeEmargement methode
) {}
