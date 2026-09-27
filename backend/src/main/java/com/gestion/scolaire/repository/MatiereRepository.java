package com.gestion.scolaire.repository;

import com.gestion.scolaire.entity.Matiere;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MatiereRepository extends JpaRepository<Matiere, Long> {

    Optional<Matiere> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByNom(String nom);
}
