package com.gestion.scolaire.service;

import com.gestion.scolaire.dto.auth.JwtResponse;
import com.gestion.scolaire.dto.auth.LoginRequest;
import com.gestion.scolaire.dto.auth.RegisterRequest;
import com.gestion.scolaire.entity.Utilisateur;
import com.gestion.scolaire.enums.StatutKyc;
import com.gestion.scolaire.exception.DuplicateResourceException;
import com.gestion.scolaire.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public JwtResponse register(RegisterRequest request) {
        if (utilisateurRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException(
                    "Un utilisateur avec l'email '" + request.email() + "' existe déjà");
        }

        Utilisateur utilisateur = Utilisateur.builder()
                .nom(request.nom())
                .prenom(request.prenom())
                .email(request.email())
                .motDePasse(passwordEncoder.encode(request.motDePasse()))
                .role(request.role())
                .cinNumero(request.cinNumero())
                .statutKyc(StatutKyc.EN_ATTENTE)
                .build();

        utilisateur = utilisateurRepository.save(utilisateur);
        log.info("New user registered: {} with role {}", utilisateur.getEmail(), utilisateur.getRole());

        String token = jwtService.generateToken(utilisateur);
        return new JwtResponse(
                token,
                utilisateur.getId(),
                utilisateur.getEmail(),
                utilisateur.getNom(),
                utilisateur.getPrenom(),
                utilisateur.getRole()
        );
    }

    public JwtResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.motDePasse())
        );

        Utilisateur utilisateur = utilisateurRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        String token = jwtService.generateToken(utilisateur);
        log.info("User logged in: {}", utilisateur.getEmail());

        return new JwtResponse(
                token,
                utilisateur.getId(),
                utilisateur.getEmail(),
                utilisateur.getNom(),
                utilisateur.getPrenom(),
                utilisateur.getRole()
        );
    }
}
