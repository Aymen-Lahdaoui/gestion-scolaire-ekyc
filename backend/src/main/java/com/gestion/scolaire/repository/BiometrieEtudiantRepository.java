package com.gestion.scolaire.repository;

import com.gestion.scolaire.entity.BiometrieEtudiant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BiometrieEtudiantRepository extends JpaRepository<BiometrieEtudiant, Long> {

    Optional<BiometrieEtudiant> findByEtudiantId(Long etudiantId);

    boolean existsByEtudiantId(Long etudiantId);

    /**
     * Finds the closest face embedding using pgvector cosine distance operator (<=>) .
     * Returns a projection containing etudiant_id and the cosine distance.
     *
     * @param vector the query vector as a PostgreSQL vector literal string, e.g. "[0.1, 0.2, ...]"
     * @return list of object arrays [etudiant_id (Long), distance (Double)]
     */
    @Query(value = """
            SELECT e.etudiant_id, (e.face_embedding <=> CAST(:vector AS vector)) AS distance
            FROM biometrie_etudiants e
            ORDER BY distance ASC
            LIMIT 1
            """, nativeQuery = true)
    List<Object[]> findClosestEmbedding(@Param("vector") String vector);

    /**
     * Finds all embeddings within a given cosine distance threshold.
     */
    @Query(value = """
            SELECT e.etudiant_id, (e.face_embedding <=> CAST(:vector AS vector)) AS distance
            FROM biometrie_etudiants e
            WHERE (e.face_embedding <=> CAST(:vector AS vector)) < :threshold
            ORDER BY distance ASC
            """, nativeQuery = true)
    List<Object[]> findEmbeddingsWithinThreshold(@Param("vector") String vector,
                                                  @Param("threshold") double threshold);
}
