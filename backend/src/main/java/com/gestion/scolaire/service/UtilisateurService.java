package com.gestion.scolaire.service;

import com.gestion.scolaire.dto.utilisateur.UtilisateurCreateDto;
import com.gestion.scolaire.dto.utilisateur.UtilisateurDto;
import com.gestion.scolaire.entity.Utilisateur;
import com.gestion.scolaire.enums.Role;
import com.gestion.scolaire.enums.StatutKyc;
import com.gestion.scolaire.exception.DuplicateResourceException;
import com.gestion.scolaire.exception.ResourceNotFoundException;
import com.gestion.scolaire.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UtilisateurDto> findAll() {
        return utilisateurRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public UtilisateurDto findById(Long id) {
        return toDto(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<UtilisateurDto> findByRole(Role role) {
        return utilisateurRepository.findByRole(role)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public UtilisateurDto create(UtilisateurCreateDto dto) {
        if (utilisateurRepository.existsByEmail(dto.email())) {
            throw new DuplicateResourceException(
                    "Un utilisateur avec l'email '" + dto.email() + "' existe déjà");
        }
        Utilisateur utilisateur = Utilisateur.builder()
                .nom(dto.nom())
                .prenom(dto.prenom())
                .email(dto.email())
                .motDePasse(passwordEncoder.encode(dto.motDePasse()))
                .role(dto.role())
                .cinNumero(dto.cinNumero())
                .statutKyc(StatutKyc.EN_ATTENTE)
                .build();
        return toDto(utilisateurRepository.save(utilisateur));
    }

    @Transactional
    public UtilisateurDto update(Long id, UtilisateurCreateDto dto) {
        Utilisateur utilisateur = getOrThrow(id);
        if (!utilisateur.getEmail().equals(dto.email())
                && utilisateurRepository.existsByEmail(dto.email())) {
            throw new DuplicateResourceException(
                    "Un utilisateur avec l'email '" + dto.email() + "' existe déjà");
        }
        utilisateur.setNom(dto.nom());
        utilisateur.setPrenom(dto.prenom());
        utilisateur.setEmail(dto.email());
        utilisateur.setRole(dto.role());
        utilisateur.setCinNumero(dto.cinNumero());
        if (dto.motDePasse() != null && !dto.motDePasse().isBlank()) {
            utilisateur.setMotDePasse(passwordEncoder.encode(dto.motDePasse()));
        }
        return toDto(utilisateurRepository.save(utilisateur));
    }

    @Transactional
    public void delete(Long id) {
        Utilisateur utilisateur = getOrThrow(id);
        utilisateurRepository.delete(utilisateur);
        log.info("Deleted user id={}", id);
    }

    public Utilisateur getOrThrow(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", id));
    }

    public UtilisateurDto toDto(Utilisateur u) {
        return new UtilisateurDto(
                u.getId(),
                u.getNom(),
                u.getPrenom(),
                u.getEmail(),
                u.getRole(),
                u.getCinNumero(),
                u.getStatutKyc(),
                u.getCreatedAt()
        );
    }
}
