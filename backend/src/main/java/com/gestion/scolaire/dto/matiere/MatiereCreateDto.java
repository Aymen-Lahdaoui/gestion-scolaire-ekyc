package com.gestion.scolaire.dto.matiere;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MatiereCreateDto(
        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 200)
        String nom,

        @NotBlank(message = "Le code est obligatoire")
        @Size(max = 50)
        String code,

        String description
) {}
