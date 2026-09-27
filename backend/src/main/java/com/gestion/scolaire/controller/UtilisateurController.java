package com.gestion.scolaire.controller;

import com.gestion.scolaire.dto.utilisateur.UtilisateurCreateDto;
import com.gestion.scolaire.dto.utilisateur.UtilisateurDto;
import com.gestion.scolaire.enums.Role;
import com.gestion.scolaire.service.UtilisateurService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
@Tag(name = "Utilisateurs", description = "Gestion des utilisateurs (ADMIN)")
@SecurityRequirement(name = "bearerAuth")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    @Operation(summary = "Lister tous les utilisateurs")
    public ResponseEntity<List<UtilisateurDto>> findAll() {
        return ResponseEntity.ok(utilisateurService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR') or #id == authentication.principal.id")
    @Operation(summary = "Obtenir un utilisateur par son ID")
    public ResponseEntity<UtilisateurDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(utilisateurService.findById(id));
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    @Operation(summary = "Lister les utilisateurs par rôle")
    public ResponseEntity<List<UtilisateurDto>> findByRole(@PathVariable Role role) {
        return ResponseEntity.ok(utilisateurService.findByRole(role));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    @Operation(summary = "Créer un utilisateur")
    public ResponseEntity<UtilisateurDto> create(@Valid @RequestBody UtilisateurCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(utilisateurService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    @Operation(summary = "Mettre à jour un utilisateur")
    public ResponseEntity<UtilisateurDto> update(@PathVariable Long id,
                                                  @Valid @RequestBody UtilisateurCreateDto dto) {
        return ResponseEntity.ok(utilisateurService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    @Operation(summary = "Supprimer un utilisateur")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        utilisateurService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
