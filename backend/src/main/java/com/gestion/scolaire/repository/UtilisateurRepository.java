package com.gestion.scolaire.repository;

import com.gestion.scolaire.entity.Utilisateur;
import com.gestion.scolaire.enums.Role;
import com.gestion.scolaire.enums.StatutKyc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Utilisateur> findByRole(Role role);

    List<Utilisateur> findByStatutKyc(StatutKyc statutKyc);

    List<Utilisateur> findByRoleAndStatutKyc(Role role, StatutKyc statutKyc);
}
