# CCAM

Sistema web para gestão de academias de pequeno e médio porte. O aluno usa o app: https://github.com/marinalivias/ccam-app

Feito com Java 17, Spring Boot, PostgreSQL, HTML, CSS e JavaScript.

## Logins de teste

| Onde | Quem | E-mail | Senha |
| --- | --- | --- | --- |
| Site | Gestora Ana Paula Moreira | `ana.moreira@formulafitness.com` | `teste123` |
| App | Aluno Bruno Henrique Costa | `bruno.costa@exemplo.com` | `teste123` |

Com os dois dá para mostrar o sistema inteiro: alunos, treinos, financeiro com Pix, mural, CRM, loja e usuários
no site; treino com GIF, calendário, mensalidades, notificações e evolução no app.

O aluno de teste sempre faz aniversário no dia em que o sistema é ligado, para aparecer o desconto da loja.
Esses dados são preparados ao ligar o sistema. Para desligar, use `DEMO=false` no `.env`.

O cadastro de academias e as mensagens do "Fale conosco" ficam com o administrador (`ADMIN_EMAIL` e `ADMIN_SENHA` do `.env`).

## Como rodar

1. Crie um banco no PostgreSQL chamado `ccam`.
2. Copie o `.env.example` para `.env` e preencha a senha do banco.
3. Rode `./mvnw spring-boot:run` e abra http://localhost:8081

## Pix

O gestor cadastra a chave Pix da academia na tela de financeiro. O aluno gera o QR code no app, paga no banco
e avisa que pagou. O pagamento aparece no site como informado e o responsável só confirma.

## Perfis

- Administrador: cadastra as academias.
- Gestor: controla tudo da academia, inclusive a loja. Só ele altera ou exclui lançamentos do financeiro.
- Professor: consulta alunos e monta os treinos.
- Atendente: cadastra alunos, lança mensalidades e publica avisos.

As fotos dos exercícios vêm do [free-exercise-db](https://github.com/yuhonas/free-exercise-db), de domínio público.
