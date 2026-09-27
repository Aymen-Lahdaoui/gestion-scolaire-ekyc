package com.gestion.scolaire.dto.emargement;

import com.gestion.scolaire.enums.MethodeEmargement;
import com.gestion.scolaire.enums.StatutEmargement;

import java.time.LocalDateTime;

public record EmargementDto(
        Long id,
        Long seanceId,
        Long etudiantId,
        String etudiantNom,
        String etudiantPrenom,
        StatutEmargement statut,
        MethodeEmargement methode,
        LocalDateTime horodatage
) {}
