package com.gestion.scolaire.dto.utilisateur;

import com.gestion.scolaire.enums.Role;
import jakarta.validation.constraints.*;

public record UtilisateurCreateDto(
        @NotBlank @Size(max = 100) String nom,
        @NotBlank @Size(max = 100) String prenom,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8) String motDePasse,
        @NotNull Role role,
        @Size(max = 50) String cinNumero
) {}
