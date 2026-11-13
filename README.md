# CCAM

Sistema web para gestão de academias de pequeno e médio porte.

## Tecnologias

- Java 17 e Spring Boot
- PostgreSQL
- HTML, CSS e JavaScript

## Como rodar

1. Crie um banco no PostgreSQL chamado `ccam`.
2. Copie o `.env.example` para `.env` e preencha a senha do banco.
3. Rode:

```
./mvnw spring-boot:run
```

4. Abra http://localhost:8081

As tabelas são criadas na primeira execução, junto com o administrador e o gestor definidos no `.env`.

## Login de exemplo

| Perfil | E-mail | Senha |
|---|---|---|
| Administrador | admin@ccam.com | admin123 |
| Gestor | gestor@formulafitness.com | gestor123 |

Troque as senhas em "Minha conta" se for usar fora do computador local.

## Perfis

- **Administrador:** cadastra as academias.
- **Gestor:** controla tudo da academia.
- **Professor:** consulta alunos e monta os treinos.
- **Atendente:** cadastra alunos, lança mensalidades e publica avisos.

Só o gestor pode alterar ou excluir lançamentos do financeiro.
