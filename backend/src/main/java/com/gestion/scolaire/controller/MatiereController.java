package com.gestion.scolaire.controller;

import com.gestion.scolaire.dto.matiere.MatiereCreateDto;
import com.gestion.scolaire.dto.matiere.MatiereDto;
import com.gestion.scolaire.service.MatiereService;
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
@RequestMapping("/api/matieres")
@RequiredArgsConstructor
@Tag(name = "Matières", description = "Gestion des matières")
@SecurityRequirement(name = "bearerAuth")
public class MatiereController {

    private final MatiereService matiereService;

    @GetMapping
    @Operation(summary = "Lister toutes les matières")
    public ResponseEntity<List<MatiereDto>> findAll() {
        return ResponseEntity.ok(matiereService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir une matière par son ID")
    public ResponseEntity<MatiereDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(matiereService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    @Operation(summary = "Créer une matière")
    public ResponseEntity<MatiereDto> create(@Valid @RequestBody MatiereCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(matiereService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    @Operation(summary = "Mettre à jour une matière")
    public ResponseEntity<MatiereDto> update(@PathVariable Long id,
                                              @Valid @RequestBody MatiereCreateDto dto) {
        return ResponseEntity.ok(matiereService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    @Operation(summary = "Supprimer une matière")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        matiereService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
