UPDATE catalogo_exercicios c SET nome = v.novo
FROM (VALUES
    ('Flexao de braco', 'Flexão de braço'),
    ('Panturrilha em pe', 'Panturrilha em pé'),
    ('Elevacao lateral', 'Elevação lateral'),
    ('Elevacao frontal', 'Elevação frontal'),
    ('Triceps corda', 'Tríceps corda'),
    ('Triceps testa', 'Tríceps testa'),
    ('Triceps frances', 'Tríceps francês'),
    ('Elevacao pelvica', 'Elevação pélvica')
) AS v(antigo, novo)
WHERE c.nome = v.antigo
  AND NOT EXISTS (SELECT 1 FROM catalogo_exercicios d WHERE d.nome = v.novo);

DELETE FROM catalogo_exercicios c
USING (VALUES
    ('Flexao de braco', 'Flexão de braço'),
    ('Panturrilha em pe', 'Panturrilha em pé'),
    ('Elevacao lateral', 'Elevação lateral'),
    ('Elevacao frontal', 'Elevação frontal'),
    ('Triceps corda', 'Tríceps corda'),
    ('Triceps testa', 'Tríceps testa'),
    ('Triceps frances', 'Tríceps francês'),
    ('Elevacao pelvica', 'Elevação pélvica')
) AS v(antigo, novo)
WHERE c.nome = v.antigo;

UPDATE catalogo_exercicios c SET grupo_muscular = v.novo
FROM (VALUES
    ('Biceps', 'Bíceps'),
    ('Triceps', 'Tríceps'),
    ('Abdomen', 'Abdômen'),
    ('Gluteos', 'Glúteos')
) AS v(antigo, novo)
WHERE c.grupo_muscular = v.antigo;

UPDATE exercicios e SET nome_exercicio = v.novo
FROM (VALUES
    ('Flexao de braco', 'Flexão de braço'),
    ('Panturrilha em pe', 'Panturrilha em pé'),
    ('Elevacao lateral', 'Elevação lateral'),
    ('Elevacao frontal', 'Elevação frontal'),
    ('Triceps corda', 'Tríceps corda'),
    ('Triceps testa', 'Tríceps testa'),
    ('Triceps frances', 'Tríceps francês'),
    ('Elevacao pelvica', 'Elevação pélvica')
) AS v(antigo, novo)
WHERE e.nome_exercicio = v.antigo;

UPDATE exercicios e SET grupo_muscular = v.novo
FROM (VALUES
    ('Biceps', 'Bíceps'),
    ('Triceps', 'Tríceps'),
    ('Abdomen', 'Abdômen'),
    ('Gluteos', 'Glúteos')
) AS v(antigo, novo)
WHERE e.grupo_muscular = v.antigo;

INSERT INTO catalogo_exercicios (nome, grupo_muscular)
SELECT v.nome, v.grupo
FROM (VALUES
    ('Supino reto', 'Peito'),
    ('Supino inclinado', 'Peito'),
    ('Crucifixo', 'Peito'),
    ('Peck deck', 'Peito'),
    ('Flexão de braço', 'Peito'),
    ('Puxada frontal', 'Costas'),
    ('Remada curvada', 'Costas'),
    ('Remada baixa', 'Costas'),
    ('Barra fixa', 'Costas'),
    ('Pulldown', 'Costas'),
    ('Agachamento livre', 'Pernas'),
    ('Leg press 45', 'Pernas'),
    ('Cadeira extensora', 'Pernas'),
    ('Mesa flexora', 'Pernas'),
    ('Afundo', 'Pernas'),
    ('Panturrilha em pé', 'Pernas'),
    ('Desenvolvimento com halteres', 'Ombros'),
    ('Elevação lateral', 'Ombros'),
    ('Elevação frontal', 'Ombros'),
    ('Crucifixo inverso', 'Ombros'),
    ('Rosca direta', 'Bíceps'),
    ('Rosca alternada', 'Bíceps'),
    ('Rosca martelo', 'Bíceps'),
    ('Tríceps corda', 'Tríceps'),
    ('Tríceps testa', 'Tríceps'),
    ('Tríceps francês', 'Tríceps'),
    ('Abdominal supra', 'Abdômen'),
    ('Prancha', 'Abdômen'),
    ('Abdominal infra', 'Abdômen'),
    ('Elevação pélvica', 'Glúteos'),
    ('Cadeira abdutora', 'Glúteos'),
    ('Stiff', 'Glúteos')
) AS v(nome, grupo)
WHERE NOT EXISTS (
    SELECT 1 FROM catalogo_exercicios c WHERE c.nome = v.nome
);
