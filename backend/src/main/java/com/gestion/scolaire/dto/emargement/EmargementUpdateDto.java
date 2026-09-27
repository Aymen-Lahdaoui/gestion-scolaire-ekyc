package com.gestion.scolaire.dto.emargement;

import com.gestion.scolaire.enums.MethodeEmargement;
import com.gestion.scolaire.enums.StatutEmargement;
import jakarta.validation.constraints.NotNull;

public record EmargementUpdateDto(
        @NotNull(message = "Le statut est obligatoire")
        StatutEmargement statut,

        @NotNull(message = "La méthode est obligatoire")
        MethodeEmargement methode
) {}
