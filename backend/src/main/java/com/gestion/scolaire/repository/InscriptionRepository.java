package com.gestion.scolaire.repository;

import com.gestion.scolaire.entity.Inscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InscriptionRepository extends JpaRepository<Inscription, Long> {

    List<Inscription> findByEtudiantId(Long etudiantId);

    List<Inscription> findByMatiereId(Long matiereId);

    Optional<Inscription> findByEtudiantIdAndMatiereId(Long etudiantId, Long matiereId);

    boolean existsByEtudiantIdAndMatiereId(Long etudiantId, Long matiereId);

    @Query("""
            SELECT i FROM Inscription i
            JOIN FETCH i.etudiant
            JOIN FETCH i.matiere
            WHERE i.matiere.id = :matiereId
            """)
    List<Inscription> findByMatiereIdWithDetails(@Param("matiereId") Long matiereId);
}
