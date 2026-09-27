package com.gestion.scolaire.controller;

import com.gestion.scolaire.service.StatistiquesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
@Tag(name = "Statistiques", description = "Statistiques de présence")
@SecurityRequirement(name = "bearerAuth")
public class StatistiquesController {

    private final StatistiquesService statistiquesService;

    @GetMapping("/presence/global")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    @Operation(summary = "Statistiques globales de présence")
    public ResponseEntity<Map<String, Object>> globalStats() {
        return ResponseEntity.ok(statistiquesService.statistiquesGlobales());
    }

    @GetMapping("/presence/matiere/{matiereId}")
    @PreAuthorize("hasRole('ADMINISTRATEUR') or hasRole('ENSEIGNANT')")
    @Operation(summary = "Taux de présence pour une matière")
    public ResponseEntity<Map<String, Object>> presenceParMatiere(@PathVariable Long matiereId) {
        return ResponseEntity.ok(statistiquesService.tauxPresenceParMatiere(matiereId));
    }

    @GetMapping("/presence/toutes-matieres")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    @Operation(summary = "Taux de présence pour toutes les matières")
    public ResponseEntity<List<Map<String, Object>>> presenceToutesLesMatiere() {
        return ResponseEntity.ok(statistiquesService.tauxPresenceToutesLesMatiere());
    }

    @GetMapping("/presence/etudiant/{etudiantId}/matiere/{matiereId}")
    @PreAuthorize("hasRole('ADMINISTRATEUR') or hasRole('ENSEIGNANT') or #etudiantId == authentication.principal.id")
    @Operation(summary = "Taux de présence d'un étudiant dans une matière")
    public ResponseEntity<Map<String, Object>> presenceEtudiantMatiere(
            @PathVariable Long etudiantId,
            @PathVariable Long matiereId) {
        return ResponseEntity.ok(
                statistiquesService.tauxPresenceEtudiantParMatiere(etudiantId, matiereId));
    }
}
