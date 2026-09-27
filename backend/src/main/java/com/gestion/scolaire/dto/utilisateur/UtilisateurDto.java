package com.gestion.scolaire.dto.utilisateur;

import com.gestion.scolaire.enums.Role;
import com.gestion.scolaire.enums.StatutKyc;

import java.time.LocalDateTime;

public record UtilisateurDto(
        Long id,
        String nom,
        String prenom,
        String email,
        Role role,
        String cinNumero,
        StatutKyc statutKyc,
        LocalDateTime createdAt
) {}
