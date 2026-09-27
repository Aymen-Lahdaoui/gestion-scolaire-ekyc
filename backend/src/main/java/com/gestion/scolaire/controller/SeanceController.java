package com.gestion.scolaire.controller;

import com.gestion.scolaire.dto.seance.SeanceCreateDto;
import com.gestion.scolaire.dto.seance.SeanceDto;
import com.gestion.scolaire.dto.utilisateur.UtilisateurDto;
import com.gestion.scolaire.service.SeanceService;
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
@RequestMapping("/api/seances")
@RequiredArgsConstructor
@Tag(name = "Séances", description = "Gestion des séances de cours")
@SecurityRequirement(name = "bearerAuth")
public class SeanceController {

    private final SeanceService seanceService;

    @GetMapping
    @Operation(summary = "Lister toutes les séances")
    public ResponseEntity<List<SeanceDto>> findAll() {
        return ResponseEntity.ok(seanceService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir une séance par son ID")
    public ResponseEntity<SeanceDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(seanceService.findById(id));
    }

    @GetMapping("/professeur/{professeurId}")
    @Operation(summary = "Lister les séances d'un professeur")
    public ResponseEntity<List<SeanceDto>> findByProfesseur(@PathVariable Long professeurId) {
        return ResponseEntity.ok(seanceService.findByProfesseur(professeurId));
    }

    @GetMapping("/matiere/{matiereId}")
    @Operation(summary = "Lister les séances d'une matière")
    public ResponseEntity<List<SeanceDto>> findByMatiere(@PathVariable Long matiereId) {
        return ResponseEntity.ok(seanceService.findByMatiere(matiereId));
    }

    @GetMapping("/{id}/students")
    @PreAuthorize("hasRole('ADMINISTRATEUR') or hasRole('ENSEIGNANT')")
    @Operation(summary = "Lister les étudiants inscrits à la séance")
    public ResponseEntity<List<UtilisateurDto>> getStudents(@PathVariable Long id) {
        return ResponseEntity.ok(seanceService.getStudentsForSeance(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATEUR') or hasRole('ENSEIGNANT')")
    @Operation(summary = "Créer une séance")
    public ResponseEntity<SeanceDto> create(@Valid @RequestBody SeanceCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(seanceService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR') or hasRole('ENSEIGNANT')")
    @Operation(summary = "Mettre à jour une séance")
    public ResponseEntity<SeanceDto> update(@PathVariable Long id,
                                             @Valid @RequestBody SeanceCreateDto dto) {
        return ResponseEntity.ok(seanceService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    @Operation(summary = "Supprimer une séance")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        seanceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/launch")
    @PreAuthorize("hasRole('ADMINISTRATEUR') or hasRole('ENSEIGNANT')")
    @Operation(summary = "Lancer une séance (PLANIFIEE -> EN_COURS)")
    public ResponseEntity<SeanceDto> launch(@PathVariable Long id) {
        return ResponseEntity.ok(seanceService.lancerSeance(id));
    }

    @PutMapping("/{id}/close")
    @PreAuthorize("hasRole('ADMINISTRATEUR') or hasRole('ENSEIGNANT')")
    @Operation(summary = "Fermer une séance (-> TERMINEE, absences enregistrées)")
    public ResponseEntity<SeanceDto> close(@PathVariable Long id) {
        return ResponseEntity.ok(seanceService.fermerSeance(id));
    }
}
