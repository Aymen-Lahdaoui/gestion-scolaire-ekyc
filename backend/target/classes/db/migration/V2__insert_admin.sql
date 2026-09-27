-- Insert default admin user
-- Password: Admin@123 (BCrypt hash)
INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, role, cin_numero, statut_kyc, created_at)
VALUES (
    'Admin',
    'System',
    'admin@gestion-scolaire.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LeXHoQqJw7pZiHhJy',
    'ADMINISTRATEUR',
    'ADMIN000',
    'VALIDE',
    NOW()
);

-- Insert sample teacher
INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, role, cin_numero, statut_kyc, created_at)
VALUES (
    'Dupont',
    'Marie',
    'marie.dupont@gestion-scolaire.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LeXHoQqJw7pZiHhJy',
    'ENSEIGNANT',
    'ENS001',
    'VALIDE',
    NOW()
);

-- Insert sample subject
INSERT INTO matieres (nom, code, description)
VALUES ('Mathématiques', 'MATH101', 'Cours de mathématiques fondamentales');

INSERT INTO matieres (nom, code, description)
VALUES ('Informatique', 'INFO101', 'Introduction à la programmation');
