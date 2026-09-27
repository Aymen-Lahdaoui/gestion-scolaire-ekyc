package com.gestion.scolaire.entity;

import com.gestion.scolaire.enums.MethodeEmargement;
import com.gestion.scolaire.enums.StatutEmargement;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "emargements",
       uniqueConstraints = @UniqueConstraint(columnNames = {"seance_id", "etudiant_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Emargement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seance_id", nullable = false)
    private Seance seance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etudiant_id", nullable = false)
    private Utilisateur etudiant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private StatutEmargement statut = StatutEmargement.ABSENT;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private MethodeEmargement methode;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime horodatage = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (horodatage == null) {
            horodatage = LocalDateTime.now();
        }
        if (statut == null) {
            statut = StatutEmargement.ABSENT;
        }
    }
}
