# CCAM

Sistema web para gestão de academias de pequeno e médio porte. O aluno usa o app: https://github.com/marinalivias/ccam-app

Feito com Java 17, Spring Boot, PostgreSQL, HTML, CSS e JavaScript.

## Logins para a apresentação

> **Site (gestor):** `gestor@formulafitness.com` / `gestor123`
>
> **App (aluno):** `aluno@ccam.com` / `11144477735`

Com esses dois dá para mostrar tudo:

- **Gestor no site:** alunos, treinos, financeiro (pagas, a receber e vencidas), mural, CRM com frequência e aniversariantes, loja e usuários.
- **Aluno no app:** treino com GIF e registro por série, semana, ficha própria, mural, frequência, mensalidades e loja.
- O aluno de exemplo **sempre faz aniversário no dia em que o sistema é ligado**, junto com uma colega. Assim aparecem o desconto da loja, o botão de parabéns e a notificação de parabéns recebida.

Esses dados são preparados sozinhos ao ligar o sistema. Para desligar, use `DEMO=false` no `.env`.

O cadastro de academias e as mensagens do "Fale conosco" ficam com o administrador: `admin@ccam.com` / `admin123`.

## Como rodar

1. Crie um banco no PostgreSQL chamado `ccam`.
2. Copie o `.env.example` para `.env` e preencha a senha do banco.
3. Rode `./mvnw spring-boot:run` e abra http://localhost:8081

## Perfis

- **Administrador:** cadastra as academias.
- **Gestor:** controla tudo da academia, inclusive a loja. Só ele altera ou exclui lançamentos do financeiro.
- **Professor:** consulta alunos e monta os treinos.
- **Atendente:** cadastra alunos, lança mensalidades e publica avisos.

As fotos dos exercícios vêm do [free-exercise-db](https://github.com/yuhonas/free-exercise-db), de domínio público.
