package com.gestion.scolaire.repository;

import com.gestion.scolaire.entity.Emargement;
import com.gestion.scolaire.enums.StatutEmargement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmargementRepository extends JpaRepository<Emargement, Long> {

    List<Emargement> findBySeanceId(Long seanceId);

    List<Emargement> findByEtudiantId(Long etudiantId);

    Optional<Emargement> findBySeanceIdAndEtudiantId(Long seanceId, Long etudiantId);

    boolean existsBySeanceIdAndEtudiantId(Long seanceId, Long etudiantId);

    List<Emargement> findBySeanceIdAndStatut(Long seanceId, StatutEmargement statut);

    @Query("""
            SELECT e FROM Emargement e
            JOIN FETCH e.etudiant
            JOIN FETCH e.seance s
            JOIN FETCH s.matiere
            WHERE s.id = :seanceId
            ORDER BY e.etudiant.nom, e.etudiant.prenom
            """)
    List<Emargement> findBySeanceIdWithDetails(@Param("seanceId") Long seanceId);

    @Query("""
            SELECT e FROM Emargement e
            JOIN FETCH e.seance s
            WHERE e.etudiant.id = :etudiantId
              AND s.matiere.id = :matiereId
            ORDER BY s.heureDebut
            """)
    List<Emargement> findByEtudiantAndMatiere(@Param("etudiantId") Long etudiantId,
                                               @Param("matiereId") Long matiereId);

    @Query("""
            SELECT COUNT(e) FROM Emargement e
            WHERE e.seance.id IN (
                SELECT s.id FROM Seance s WHERE s.matiere.id = :matiereId
            )
            AND e.etudiant.id = :etudiantId
            AND e.statut = 'PRESENT'
            """)
    long countPresencesByEtudiantAndMatiere(@Param("etudiantId") Long etudiantId,
                                             @Param("matiereId") Long matiereId);

    @Query("""
            SELECT COUNT(e) FROM Emargement e
            WHERE e.seance.id IN (
                SELECT s.id FROM Seance s WHERE s.matiere.id = :matiereId
            )
            AND e.statut = 'PRESENT'
            """)
    long countPresencesByMatiere(@Param("matiereId") Long matiereId);

    @Query("""
            SELECT COUNT(e) FROM Emargement e
            WHERE e.seance.id IN (
                SELECT s.id FROM Seance s WHERE s.matiere.id = :matiereId
            )
            """)
    long countTotalByMatiere(@Param("matiereId") Long matiereId);
}
