package com.gestion.scolaire.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "biometrie_etudiants")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class BiometrieEtudiant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etudiant_id", nullable = false, unique = true)
    private Utilisateur etudiant;

    /**
     * Face embedding stored as a pgvector(512).
     * Persisted as a float[] and mapped to the vector column via custom read/write.
     */
    @Column(name = "face_embedding", columnDefinition = "vector(512)")
    private float[] faceEmbedding;

    @Column(name = "date_verification", nullable = false)
    @Builder.Default
    private LocalDateTime dateVerification = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (dateVerification == null) {
            dateVerification = LocalDateTime.now();
        }
    }
}
