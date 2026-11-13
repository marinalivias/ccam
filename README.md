# CCAM

Sistema web para gestão de academias de pequeno e médio porte. O aluno usa o app: https://github.com/marinalivias/ccam-app

Feito com Java 17, Spring Boot, PostgreSQL, HTML, CSS e JavaScript.

## Como rodar

1. Crie um banco no PostgreSQL chamado `ccam`.
2. Copie o `.env.example` para `.env` e preencha a senha do banco.
3. Rode `./mvnw spring-boot:run` e abra http://localhost:8081

Na primeira execução o sistema cria as tabelas, a academia de exemplo e os logins abaixo.

## Login de exemplo

| Perfil | E-mail | Senha |
|---|---|---|
| Administrador | admin@ccam.com | admin123 |
| Gestor | gestor@formulafitness.com | gestor123 |
| Aluno (app) | aluno@ccam.com | 11144477735 |

## Perfis

- **Administrador:** cadastra as academias.
- **Gestor:** controla tudo da academia, inclusive a loja. Só ele altera ou exclui lançamentos do financeiro.
- **Professor:** consulta alunos e monta os treinos.
- **Atendente:** cadastra alunos, lança mensalidades e publica avisos.
