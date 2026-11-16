# CCAM

Sistema web para gestão de academias de pequeno e médio porte. Os alunos usam o app: https://github.com/marinalivias/ccam-app

Feito com Java 17, Spring Boot, PostgreSQL, HTML, CSS e JavaScript.

## Logins de teste

| Onde | Quem | E-mail | Senha |
| --- | --- | --- | --- |
| Site | Gestora Ana Paula Moreira | `ana.moreira@formulafitness.com` | `teste123` |
| App | Aluno Bruno Henrique Costa | `bruno.costa@exemplo.com` | `teste123` |

Com os dois dá para mostrar o sistema inteiro. O cadastro de academias e as mensagens do "Fale conosco" ficam com o
administrador, configurado em `ADMIN_EMAIL` e `ADMIN_SENHA` no `.env`.

## Como rodar

1. Crie um banco no PostgreSQL chamado `ccam`.
2. Copie o `.env.example` para `.env` e preencha a senha do banco.
3. Rode `./mvnw spring-boot:run` e abra http://localhost:8081

No IntelliJ, rode a classe `CcamApplication`.

## Variáveis do `.env`

| Variável | Para que serve |
| --- | --- |
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | Conexão com o PostgreSQL |
| `ADMIN_EMAIL`, `ADMIN_SENHA` | Administrador criado na primeira vez que o sistema sobe |
| `GESTOR_EMAIL`, `GESTOR_SENHA` | Gestor da academia padrão |
| `ALUNO_SENHA` | Senha do aluno de teste no app |
| `DEMO` | `true` prepara os dados de demonstração a cada vez que o sistema sobe |
| `ASAAS_CHAVE` | Chave da API do Asaas para gerar boletos. Vazia, o boleto fica desligado |
| `EXIBIR_LINK_RESET` | `true` mostra o link de redefinir senha na tela, útil sem servidor de e-mail |

O `.env` não vai para o GitHub. Só o `.env.example` fica no repositório.

## O que o sistema faz

- Alunos: cadastro, inativação (nunca são apagados), histórico de alterações, foto enviada pelo app e
  redefinição da senha do app quando o aluno pede.
- Treinos: fichas por dia da semana com catálogo de exercícios e GIFs.
- Financeiro: mensalidades, Pix, boleto, pagamentos informados pelo aluno e mensalidades automáticas.
- CRM: frequência, alunos ausentes, aniversariantes e resumo financeiro do mês com gráfico dos últimos 6 meses.
- Mural de avisos, loja da academia e usuários com perfis de acesso.

## Pagamentos

O gestor cadastra a chave Pix da academia na tela de financeiro. O aluno gera o QR code no app, paga no banco
e avisa que pagou. O pagamento aparece no site como informado e o responsável só confirma.

O boleto usa o Asaas. Crie uma conta de testes em https://sandbox.asaas.com, gere a chave da API e coloque em
`ASAAS_CHAVE`. Quando o banco confirma um boleto pago, ele aparece no site como informado, igual ao Pix.

Com as mensalidades automáticas ligadas, todo dia às 6h o sistema lança a próxima mensalidade de cada aluno ativo.
Quem já paga um valor diferente continua com o próprio valor.

## Dados de demonstração

Com `DEMO=true`, o sistema prepara sozinho treinos registrados, mensalidades, avisos, chave Pix e histórico de
pagamentos. O aluno de teste sempre faz aniversário no dia em que o sistema sobe, para aparecer o desconto da loja.

## Perfis

- Administrador: cadastra as academias.
- Gestor: controla tudo da academia, inclusive a loja. Só ele altera ou exclui lançamentos do financeiro.
- Professor: consulta alunos e monta os treinos.
- Atendente: cadastra alunos, lança mensalidades e publica avisos.

As fotos dos exercícios vêm do [free-exercise-db](https://github.com/yuhonas/free-exercise-db), de domínio público.
