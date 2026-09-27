package com.gestion.scolaire.controller;

import com.gestion.scolaire.dto.utilisateur.KycStatusDto;
import com.gestion.scolaire.dto.utilisateur.UtilisateurDto;
import com.gestion.scolaire.enums.StatutKyc;
import com.gestion.scolaire.service.KycService;
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
@RequestMapping("/api/kyc")
@RequiredArgsConstructor
@Tag(name = "KYC", description = "Gestion eKYC et biométrie")
@SecurityRequirement(name = "bearerAuth")
public class KycController {

    private final KycService kycService;

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    @Operation(summary = "Lister les utilisateurs en attente de validation KYC")
    public ResponseEntity<List<UtilisateurDto>> getPending() {
        return ResponseEntity.ok(kycService.getPendingKyc());
    }

    @GetMapping("/status/{statut}")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    @Operation(summary = "Lister les utilisateurs par statut KYC")
    public ResponseEntity<List<UtilisateurDto>> getByStatus(@PathVariable StatutKyc statut) {
        return ResponseEntity.ok(kycService.getByKycStatus(statut));
    }

    @PutMapping("/{id}/validate")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    @Operation(summary = "Valider le KYC d'un utilisateur")
    public ResponseEntity<KycStatusDto> validate(@PathVariable Long id) {
        return ResponseEntity.ok(kycService.validateKyc(id));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    @Operation(summary = "Rejeter le KYC d'un utilisateur")
    public ResponseEntity<KycStatusDto> reject(@PathVariable Long id,
                                                @RequestParam(required = false) String motif) {
        return ResponseEntity.ok(kycService.rejectKyc(id, motif));
    }

    @PutMapping("/{id}/revision")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    @Operation(summary = "Mettre le KYC d'un utilisateur en révision")
    public ResponseEntity<KycStatusDto> setRevision(@PathVariable Long id) {
        return ResponseEntity.ok(kycService.setUnderRevision(id));
    }

    @PostMapping("/{id}/biometric-vector")
    @PreAuthorize("hasRole('ADMINISTRATEUR') or hasRole('ETUDIANT')")
    @Operation(summary = "Enregistrer le vecteur biométrique facial d'un étudiant")
    public ResponseEntity<Void> storeBiometricVector(@PathVariable Long id,
                                                      @RequestBody float[] embedding) {
        kycService.storeBiometricVector(id, embedding);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/facial-match")
    @PreAuthorize("hasRole('ADMINISTRATEUR') or hasRole('ENSEIGNANT')")
    @Operation(summary = "Identifier un étudiant par reconnaissance faciale")
    public ResponseEntity<Map<String, Object>> facialMatch(
            @RequestBody float[] queryEmbedding,
            @RequestParam(defaultValue = "0.4") double threshold) {
        return kycService.findMatchingStudent(queryEmbedding, threshold)
                .map(etudiantId -> {
                    Map<String, Object> body = new java.util.HashMap<>();
                    body.put("matched", true);
                    body.put("etudiantId", etudiantId);
                    return ResponseEntity.<Map<String, Object>>ok(body);
                })
                .orElseGet(() -> {
                    Map<String, Object> body = new java.util.HashMap<>();
                    body.put("matched", false);
                    return ResponseEntity.<Map<String, Object>>ok(body);
                });
    }
}
