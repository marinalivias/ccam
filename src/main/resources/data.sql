INSERT INTO catalogo_exercicios (nome, grupo_muscular)
SELECT v.nome, v.grupo
FROM (VALUES
    ('Supino reto', 'Peito'),
    ('Supino inclinado', 'Peito'),
    ('Crucifixo', 'Peito'),
    ('Peck deck', 'Peito'),
    ('Flexao de braco', 'Peito'),
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
    ('Panturrilha em pe', 'Pernas'),
    ('Desenvolvimento com halteres', 'Ombros'),
    ('Elevacao lateral', 'Ombros'),
    ('Elevacao frontal', 'Ombros'),
    ('Crucifixo inverso', 'Ombros'),
    ('Rosca direta', 'Biceps'),
    ('Rosca alternada', 'Biceps'),
    ('Rosca martelo', 'Biceps'),
    ('Triceps corda', 'Triceps'),
    ('Triceps testa', 'Triceps'),
    ('Triceps frances', 'Triceps'),
    ('Abdominal supra', 'Abdomen'),
    ('Prancha', 'Abdomen'),
    ('Abdominal infra', 'Abdomen'),
    ('Elevacao pelvica', 'Gluteos'),
    ('Cadeira abdutora', 'Gluteos'),
    ('Stiff', 'Gluteos')
) AS v(nome, grupo)
WHERE NOT EXISTS (
    SELECT 1 FROM catalogo_exercicios c WHERE c.nome = v.nome
);
