package com.gestion.scolaire.dto.auth;

import com.gestion.scolaire.enums.Role;

public record JwtResponse(
        String token,
        String type,
        Long userId,
        String email,
        String nom,
        String prenom,
        Role role
) {
    public JwtResponse(String token, Long userId, String email, String nom, String prenom, Role role) {
        this(token, "Bearer", userId, email, nom, prenom, role);
    }
}
