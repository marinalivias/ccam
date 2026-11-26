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

## Online

- Site: https://ccam.onrender.com
- App no navegador: https://ccam.onrender.com/app
- APK para Android: https://github.com/marinalivias/ccam-app/releases/latest

O servidor gratuito dorme quando fica parado. O primeiro acesso pode levar até 1 minuto.

## Como rodar no computador

### O que instalar

- Git
- Java 17 ou mais novo (JDK)
- PostgreSQL
- IntelliJ IDEA (opcional, dá para rodar pelo terminal)

### Passo a passo

1. Baixe o projeto:

```bash
git clone https://github.com/marinalivias/ccam.git
cd ccam
```

2. No PostgreSQL, crie um banco vazio chamado `ccam`. As tabelas são criadas sozinhas.
3. Copie o arquivo de configuração e coloque a senha do seu PostgreSQL em `DB_PASSWORD`:

```bash
cp .env.example .env
```

4. Rode o sistema:

```bash
./mvnw spring-boot:run
```

No Windows use `mvnw.cmd spring-boot:run`. No IntelliJ, abra a pasta e rode a classe `CcamApplication`.

5. Abra http://localhost:8081 e entre com o login da gestora. O app do aluno fica em http://localhost:8081/app

O `.env` tem senhas e não vai para o GitHub.

## Variáveis do `.env`

| Variável | Para que serve |
| --- | --- |
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | Conexão com o PostgreSQL |
| `ADMIN_EMAIL`, `ADMIN_SENHA` | Administrador criado na primeira vez que o sistema sobe |
| `GESTOR_EMAIL`, `GESTOR_SENHA` | Gestor da academia padrão |
| `ALUNO_SENHA` | Senha do aluno de teste no app |
| `DEMO` | `true` prepara os dados de demonstração a cada vez que o sistema sobe |
| `SWAGGER` | `true` libera a documentação da API em `/swagger-ui/index.html` |
| `EXIBIR_LINK_RESET` | `true` mostra o link de redefinir senha na tela, útil sem servidor de e-mail |

## O que o sistema faz

- Alunos: cadastro, inativação (nunca são apagados), histórico de alterações, foto enviada pelo app e
  redefinição da senha do app quando o aluno pede.
- Treinos: fichas por dia da semana com catálogo de exercícios e GIFs.
- Financeiro: mensalidades, Pix, pagamentos informados pelo aluno e mensalidades automáticas.
- CRM: frequência, alunos ausentes, aniversariantes e resumo financeiro do mês com gráfico dos últimos 6 meses.
- Mural de avisos, loja da academia e usuários com perfis de acesso.

## Pagamentos

O gestor cadastra a chave Pix da academia na tela de financeiro. O aluno gera o QR code no app, paga no banco
e avisa que pagou. O pagamento aparece no site como informado e o responsável só confirma.

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
