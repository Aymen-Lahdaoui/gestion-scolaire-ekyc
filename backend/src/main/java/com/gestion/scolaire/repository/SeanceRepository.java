package com.gestion.scolaire.repository;

import com.gestion.scolaire.entity.Seance;
import com.gestion.scolaire.enums.StatutSeance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SeanceRepository extends JpaRepository<Seance, Long> {

    List<Seance> findByMatiereId(Long matiereId);

    List<Seance> findByProfesseurId(Long professeurId);

    List<Seance> findByStatut(StatutSeance statut);

    List<Seance> findByProfesseurIdAndStatut(Long professeurId, StatutSeance statut);

    @Query("""
            SELECT s FROM Seance s
            WHERE s.matiere.id = :matiereId
              AND s.heureDebut >= :from
              AND s.heureFin <= :to
            ORDER BY s.heureDebut
            """)
    List<Seance> findByMatiereAndPeriod(@Param("matiereId") Long matiereId,
                                        @Param("from") LocalDateTime from,
                                        @Param("to") LocalDateTime to);
}
