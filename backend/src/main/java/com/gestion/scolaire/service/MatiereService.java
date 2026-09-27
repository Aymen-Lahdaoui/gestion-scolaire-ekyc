package com.gestion.scolaire.service;

import com.gestion.scolaire.dto.matiere.MatiereCreateDto;
import com.gestion.scolaire.dto.matiere.MatiereDto;
import com.gestion.scolaire.entity.Matiere;
import com.gestion.scolaire.exception.DuplicateResourceException;
import com.gestion.scolaire.exception.ResourceNotFoundException;
import com.gestion.scolaire.repository.MatiereRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatiereService {

    private final MatiereRepository matiereRepository;

    @Transactional(readOnly = true)
    public List<MatiereDto> findAll() {
        return matiereRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public MatiereDto findById(Long id) {
        return toDto(getOrThrow(id));
    }

    @Transactional
    public MatiereDto create(MatiereCreateDto dto) {
        if (matiereRepository.existsByCode(dto.code())) {
            throw new DuplicateResourceException(
                    "Une matière avec le code '" + dto.code() + "' existe déjà");
        }
        Matiere matiere = Matiere.builder()
                .nom(dto.nom())
                .code(dto.code().toUpperCase())
                .description(dto.description())
                .build();
        return toDto(matiereRepository.save(matiere));
    }

    @Transactional
    public MatiereDto update(Long id, MatiereCreateDto dto) {
        Matiere matiere = getOrThrow(id);
        if (!matiere.getCode().equals(dto.code()) && matiereRepository.existsByCode(dto.code())) {
            throw new DuplicateResourceException(
                    "Une matière avec le code '" + dto.code() + "' existe déjà");
        }
        matiere.setNom(dto.nom());
        matiere.setCode(dto.code().toUpperCase());
        matiere.setDescription(dto.description());
        return toDto(matiereRepository.save(matiere));
    }

    @Transactional
    public void delete(Long id) {
        Matiere matiere = getOrThrow(id);
        matiereRepository.delete(matiere);
        log.info("Deleted matiere id={}", id);
    }

    public Matiere getOrThrow(Long id) {
        return matiereRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Matière", "id", id));
    }

    public MatiereDto toDto(Matiere m) {
        return new MatiereDto(m.getId(), m.getNom(), m.getCode(), m.getDescription());
    }
}
