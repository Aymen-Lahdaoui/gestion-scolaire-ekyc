-- Enable pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;

-- Enum types
CREATE TYPE role_utilisateur AS ENUM ('ETUDIANT', 'ENSEIGNANT', 'ADMINISTRATEUR');
CREATE TYPE statut_kyc AS ENUM ('EN_ATTENTE', 'VALIDE', 'REJETE', 'EN_REVISION');
CREATE TYPE statut_seance AS ENUM ('PLANIFIEE', 'EN_COURS', 'TERMINEE');
CREATE TYPE statut_emargement AS ENUM ('PRESENT', 'ABSENT', 'RETARD');
CREATE TYPE methode_emargement AS ENUM ('FACIAL', 'MANUEL');

-- Table utilisateurs
CREATE TABLE utilisateurs (
    id           BIGSERIAL PRIMARY KEY,
    nom          VARCHAR(100) NOT NULL,
    prenom       VARCHAR(100) NOT NULL,
    email        VARCHAR(255) NOT NULL UNIQUE,
    mot_de_passe VARCHAR(255) NOT NULL,
    role         VARCHAR(50)  NOT NULL DEFAULT 'ETUDIANT',
    cin_numero   VARCHAR(50),
    statut_kyc   VARCHAR(50)  NOT NULL DEFAULT 'EN_ATTENTE',
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Table biometrie_etudiants
CREATE TABLE biometrie_etudiants (
    id                 BIGSERIAL PRIMARY KEY,
    etudiant_id        BIGINT NOT NULL REFERENCES utilisateurs(id) ON DELETE CASCADE,
    face_embedding     vector(512),
    date_verification  TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_biometrie_etudiant UNIQUE (etudiant_id)
);

-- Table matieres
CREATE TABLE matieres (
    id          BIGSERIAL PRIMARY KEY,
    nom         VARCHAR(200) NOT NULL,
    code        VARCHAR(50)  NOT NULL UNIQUE,
    description TEXT
);

-- Table seances
CREATE TABLE seances (
    id           BIGSERIAL PRIMARY KEY,
    matiere_id   BIGINT      NOT NULL REFERENCES matieres(id) ON DELETE RESTRICT,
    professeur_id BIGINT     NOT NULL REFERENCES utilisateurs(id) ON DELETE RESTRICT,
    salle        VARCHAR(100),
    heure_debut  TIMESTAMP   NOT NULL,
    heure_fin    TIMESTAMP   NOT NULL,
    statut       VARCHAR(50) NOT NULL DEFAULT 'PLANIFIEE'
);

-- Table inscriptions
CREATE TABLE inscriptions (
    id               BIGSERIAL PRIMARY KEY,
    etudiant_id      BIGINT    NOT NULL REFERENCES utilisateurs(id) ON DELETE CASCADE,
    matiere_id       BIGINT    NOT NULL REFERENCES matieres(id) ON DELETE CASCADE,
    date_inscription TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_inscription UNIQUE (etudiant_id, matiere_id)
);

-- Table emargements
CREATE TABLE emargements (
    id          BIGSERIAL PRIMARY KEY,
    seance_id   BIGINT      NOT NULL REFERENCES seances(id) ON DELETE CASCADE,
    etudiant_id BIGINT      NOT NULL REFERENCES utilisateurs(id) ON DELETE CASCADE,
    statut      VARCHAR(50) NOT NULL DEFAULT 'ABSENT',
    methode     VARCHAR(50),
    horodatage  TIMESTAMP   NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_emargement UNIQUE (seance_id, etudiant_id)
);

-- Indexes
CREATE INDEX idx_utilisateurs_email ON utilisateurs(email);
CREATE INDEX idx_utilisateurs_role ON utilisateurs(role);
CREATE INDEX idx_utilisateurs_statut_kyc ON utilisateurs(statut_kyc);
CREATE INDEX idx_seances_matiere ON seances(matiere_id);
CREATE INDEX idx_seances_professeur ON seances(professeur_id);
CREATE INDEX idx_inscriptions_etudiant ON inscriptions(etudiant_id);
CREATE INDEX idx_inscriptions_matiere ON inscriptions(matiere_id);
CREATE INDEX idx_emargements_seance ON emargements(seance_id);
CREATE INDEX idx_emargements_etudiant ON emargements(etudiant_id);
