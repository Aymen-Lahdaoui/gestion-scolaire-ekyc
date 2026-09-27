package com.gestion.scolaire.dto.utilisateur;

import com.gestion.scolaire.enums.StatutKyc;

public record KycStatusDto(
        Long utilisateurId,
        StatutKyc statutKyc,
        String motifRejet
) {}
