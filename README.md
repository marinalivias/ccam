# CCAM

Sistema web para gestão de academias de pequeno e médio porte. TCC.

A academia entra em contato, a gente cadastra a empresa e o gestor passa a usar o sistema:
alunos, treinos, financeiro, mural de avisos e usuários da equipe.

O app do aluno (Flutter) fica em outro repositório.

## Tecnologias

- Java 17 e Spring Boot
- PostgreSQL
- HTML, CSS e JavaScript

## Como rodar

1. Crie um banco no PostgreSQL chamado `ccam`.
2. Copie o `.env.example` para `.env` e preencha as senhas.
3. Rode:

```
./mvnw spring-boot:run
```

4. Abra http://localhost:8081

As tabelas são criadas na primeira execução, junto com o administrador e o gestor definidos no `.env`.

## Perfis

- **Administrador:** cadastra as academias.
- **Gestor:** controla tudo da academia.
- **Professor:** consulta alunos e monta os treinos.
- **Atendente:** cadastra alunos, lança mensalidades e publica avisos.

Só o gestor pode alterar ou excluir lançamentos do financeiro.
