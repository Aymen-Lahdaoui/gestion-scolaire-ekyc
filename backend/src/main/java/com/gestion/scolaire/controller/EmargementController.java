package com.gestion.scolaire.controller;

import com.gestion.scolaire.dto.emargement.EmargementCreateDto;
import com.gestion.scolaire.dto.emargement.EmargementDto;
import com.gestion.scolaire.dto.emargement.EmargementUpdateDto;
import com.gestion.scolaire.dto.emargement.SeanceEmargementReport;
import com.gestion.scolaire.service.EmargementService;
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
@RequestMapping("/api/emargements")
@RequiredArgsConstructor
@Tag(name = "Émargements", description = "Gestion des émargements (présences/absences)")
@SecurityRequirement(name = "bearerAuth")
public class EmargementController {

    private final EmargementService emargementService;

    @GetMapping("/seance/{seanceId}")
    @PreAuthorize("hasRole('ADMINISTRATEUR') or hasRole('ENSEIGNANT')")
    @Operation(summary = "Lister les émargements d'une séance")
    public ResponseEntity<List<EmargementDto>> findBySeance(@PathVariable Long seanceId) {
        return ResponseEntity.ok(emargementService.findBySeance(seanceId));
    }

    @GetMapping("/etudiant/{etudiantId}")
    @PreAuthorize("hasRole('ADMINISTRATEUR') or hasRole('ENSEIGNANT') or #etudiantId == authentication.principal.id")
    @Operation(summary = "Lister les émargements d'un étudiant")
    public ResponseEntity<List<EmargementDto>> findByEtudiant(@PathVariable Long etudiantId) {
        return ResponseEntity.ok(emargementService.findByEtudiant(etudiantId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATEUR') or hasRole('ENSEIGNANT')")
    @Operation(summary = "Marquer la présence/absence d'un étudiant")
    public ResponseEntity<EmargementDto> markPresent(@Valid @RequestBody EmargementCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(emargementService.markPresent(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR') or hasRole('ENSEIGNANT')")
    @Operation(summary = "Mettre à jour manuellement un émargement")
    public ResponseEntity<EmargementDto> update(@PathVariable Long id,
                                                 @Valid @RequestBody EmargementUpdateDto dto) {
        return ResponseEntity.ok(emargementService.updateManually(id, dto));
    }

    @GetMapping("/seance/{seanceId}/report")
    @PreAuthorize("hasRole('ADMINISTRATEUR') or hasRole('ENSEIGNANT')")
    @Operation(summary = "Obtenir le rapport d'émargement complet pour une séance")
    public ResponseEntity<SeanceEmargementReport> getReport(@PathVariable Long seanceId) {
        return ResponseEntity.ok(emargementService.getReport(seanceId));
    }
}
