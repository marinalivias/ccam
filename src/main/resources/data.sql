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

UPDATE catalogo_exercicios c
SET imagem_url = COALESCE(c.imagem_url, v.gif), instrucoes = COALESCE(c.instrucoes, v.passos)
FROM (VALUES
    ('Supino reto', '/exercicios/supino-reto.gif', 'Deite no banco com os pés firmes no chão e as escápulas encaixadas.
Segure a barra um pouco mais aberta que os ombros e desça até tocar levemente o peito.
Empurre a barra para cima até estender os braços, sem tirar o quadril do banco.'),
    ('Supino inclinado', '/exercicios/supino-inclinado.gif', 'Ajuste o banco entre 30 e 45 graus e apoie bem as costas.
Desça a barra controlando até a parte de cima do peito.
Empurre para cima mantendo os cotovelos levemente abaixo da linha dos ombros.'),
    ('Crucifixo', '/exercicios/crucifixo.gif', 'Deite no banco com um halter em cada mão, braços estendidos acima do peito.
Abra os braços em arco, com os cotovelos levemente dobrados, até sentir o alongamento do peito.
Volte pelo mesmo caminho, como se abraçasse uma árvore.'),
    ('Peck deck', '/exercicios/peck-deck.gif', 'Sente com as costas apoiadas e os braços na altura dos ombros.
Feche os braços à frente do corpo contraindo o peito.
Volte devagar até a posição inicial, sem deixar o peso bater.'),
    ('Flexão de braço', '/exercicios/flexao-de-braco.gif', 'Apoie as mãos no chão um pouco mais abertas que os ombros e mantenha o corpo reto.
Desça o peito até perto do chão, cotovelos a cerca de 45 graus do corpo.
Empurre o chão para subir, sem deixar o quadril cair.'),
    ('Puxada frontal', '/exercicios/puxada-frontal.gif', 'Sente com as coxas presas no apoio e segure a barra com as mãos afastadas.
Puxe a barra até a parte de cima do peito, levando os cotovelos para baixo.
Volte devagar até estender os braços.'),
    ('Remada curvada', '/exercicios/remada-curvada.gif', 'Incline o tronco à frente com os joelhos levemente dobrados e as costas retas.
Puxe a barra em direção ao umbigo, juntando as escápulas.
Desça a barra com controle até estender os braços.'),
    ('Remada baixa', '/exercicios/remada-baixa.gif', 'Sente com os pés apoiados e os joelhos levemente dobrados, coluna reta.
Puxe o pegador até a barriga, levando os cotovelos para trás.
Volte estendendo os braços sem curvar as costas.'),
    ('Barra fixa', '/exercicios/barra-fixa.gif', 'Segure a barra com as mãos afastadas e fique pendurado com os braços estendidos.
Puxe o corpo até o queixo passar da barra.
Desça devagar até estender os braços de novo.'),
    ('Pulldown', '/exercicios/pulldown.gif', 'Fique de pé de frente para a polia alta, braços estendidos segurando a barra.
Com os cotovelos quase retos, puxe a barra até a altura das coxas.
Volte devagar até a altura da cabeça.'),
    ('Agachamento livre', '/exercicios/agachamento-livre.gif', 'Apoie a barra no alto das costas e afaste os pés na largura dos ombros.
Desça levando o quadril para trás e para baixo, joelhos na direção dos pés.
Suba empurrando o chão, mantendo o peito aberto e as costas retas.'),
    ('Leg press 45', '/exercicios/leg-press-45.gif', 'Sente com as costas apoiadas e os pés no meio da plataforma.
Desça a plataforma dobrando os joelhos até cerca de 90 graus.
Empurre de volta sem travar totalmente os joelhos no final.'),
    ('Cadeira extensora', '/exercicios/cadeira-extensora.gif', 'Sente com as costas apoiadas e o rolo na frente da canela, perto do tornozelo.
Estenda os joelhos até as pernas ficarem retas.
Desça devagar sem deixar o peso bater.'),
    ('Mesa flexora', '/exercicios/mesa-flexora.gif', 'Deite de bruços com o rolo logo acima dos calcanhares.
Dobre os joelhos trazendo os calcanhares em direção ao glúteo.
Volte devagar até quase estender as pernas.'),
    ('Afundo', '/exercicios/afundo.gif', 'Fique em pé com um halter em cada mão e dê um passo largo à frente.
Desça até o joelho de trás quase tocar o chão, tronco reto.
Empurre com a perna da frente para voltar e troque de lado.'),
    ('Panturrilha em pé', '/exercicios/panturrilha-em-pe.gif', 'Fique em pé com a ponta dos pés apoiada e os calcanhares livres.
Suba na ponta dos pés o máximo que conseguir.
Desça devagar até alongar a panturrilha.'),
    ('Desenvolvimento com halteres', '/exercicios/desenvolvimento-com-halteres.gif', 'Sente com as costas apoiadas e os halteres na altura dos ombros.
Empurre os halteres para cima até quase estender os braços.
Desça com controle até a altura das orelhas.'),
    ('Elevação lateral', '/exercicios/elevacao-lateral.gif', 'Fique em pé com um halter em cada mão ao lado do corpo.
Eleve os braços para os lados até a altura dos ombros, cotovelos levemente dobrados.
Desça devagar, sem balançar o corpo.'),
    ('Elevação frontal', '/exercicios/elevacao-frontal.gif', 'Fique em pé com os halteres na frente das coxas.
Eleve um ou os dois braços à frente até a altura dos ombros.
Desça com controle, sem jogar o tronco para trás.'),
    ('Crucifixo inverso', '/exercicios/crucifixo-inverso.gif', 'Incline o tronco à frente com um halter em cada mão, braços para baixo.
Abra os braços para os lados até a altura dos ombros, juntando as escápulas.
Volte devagar à posição inicial.'),
    ('Rosca direta', '/exercicios/rosca-direta.gif', 'Fique em pé segurando a barra com as palmas para cima, cotovelos colados ao corpo.
Dobre os cotovelos trazendo a barra até perto dos ombros.
Desça devagar sem balançar o tronco.'),
    ('Rosca alternada', '/exercicios/rosca-alternada.gif', 'Fique em pé com um halter em cada mão, palmas viradas para o corpo.
Suba um halter girando o punho para cima, sem mexer o cotovelo.
Desça devagar e repita com o outro braço.'),
    ('Rosca martelo', '/exercicios/rosca-martelo.gif', 'Fique em pé com os halteres ao lado do corpo, palmas viradas uma para a outra.
Dobre os cotovelos mantendo essa pegada, como um martelo.
Desça devagar até estender os braços.'),
    ('Tríceps corda', '/exercicios/triceps-corda.gif', 'De frente para a polia alta, segure a corda com os cotovelos colados ao corpo.
Empurre a corda para baixo abrindo as pontas no final.
Volte até os antebraços ficarem paralelos ao chão.'),
    ('Tríceps testa', '/exercicios/triceps-testa.gif', 'Deite no banco segurando a barra com os braços estendidos acima do peito.
Dobre só os cotovelos, levando a barra até perto da testa.
Estenda os braços de novo sem abrir os cotovelos.'),
    ('Tríceps francês', '/exercicios/triceps-frances.gif', 'Sente ou fique em pé segurando um halter com as duas mãos acima da cabeça.
Desça o halter atrás da cabeça dobrando os cotovelos.
Estenda os braços de volta, cotovelos apontando para cima.'),
    ('Abdominal supra', '/exercicios/abdominal-supra.gif', 'Deite com os joelhos dobrados e os pés no chão, mãos atrás da cabeça.
Tire os ombros do chão contraindo o abdômen, sem puxar o pescoço.
Desça devagar até as costas encostarem.'),
    ('Prancha', '/exercicios/prancha.gif', 'Apoie os antebraços e as pontas dos pés no chão.
Mantenha o corpo em linha reta, abdômen e glúteos contraídos.
Segure a posição pelo tempo indicado, respirando normalmente.'),
    ('Abdominal infra', '/exercicios/abdominal-infra.gif', 'Deite de costas com as pernas dobradas e as mãos ao lado do corpo.
Tire o quadril do chão trazendo os joelhos em direção ao peito.
Desça devagar sem deixar os pés baterem no chão.'),
    ('Elevação pélvica', '/exercicios/elevacao-pelvica.gif', 'Apoie as costas no banco com os pés no chão e o peso sobre o quadril.
Eleve o quadril até o corpo ficar reto dos ombros aos joelhos.
Contraia os glúteos no alto e desça com controle.'),
    ('Cadeira abdutora', '/exercicios/cadeira-abdutora.gif', 'Sente com as costas apoiadas e as almofadas do lado de fora dos joelhos.
Afaste as pernas abrindo para os lados.
Volte devagar sem deixar o peso bater.'),
    ('Stiff', '/exercicios/stiff.gif', 'Fique em pé segurando a barra na frente das coxas, joelhos levemente dobrados.
Desça a barra perto das pernas levando o quadril para trás, costas retas.
Suba contraindo os glúteos até ficar ereto.')
) AS v(nome, gif, passos)
WHERE c.nome = v.nome;

UPDATE exercicios e SET imagem_url = c.imagem_url
FROM catalogo_exercicios c
WHERE e.nome_exercicio = c.nome AND e.imagem_url IS NULL AND c.imagem_url IS NOT NULL;
