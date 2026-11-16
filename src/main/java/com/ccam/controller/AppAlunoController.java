package com.ccam.controller;

import com.ccam.dto.*;
import com.ccam.exception.NegocioException;
import com.ccam.model.*;
import com.ccam.repository.*;
import com.ccam.security.AlunoInterceptor;
import com.ccam.security.AuthInterceptor;
import com.ccam.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/app")
@CrossOrigin(origins = "*")
public class AppAlunoController {
    private final AlunoAuthService authService;
    private final ExercicioRepository exercicios;
    private final CatalogoExercicioRepository catalogo;
    private final TreinoRealizadoRepository realizados;
    private final MuralRepository mural;
    private final FinanceiroRepository financeiro;
    private final ClienteRepository clientes;
    private final FrequenciaService frequencia;
    private final LogService logService;
    private final ProdutoRepository produtos;
    private final LojaService lojaService;
    private final TreinoRealizadoService treinoRealizado;
    private final PixService pixService;
    private final com.ccam.service.NotificacaoService notificacoes;
    private final com.ccam.service.FotoService fotoService;
    private final com.ccam.repository.EmpresaRepository empresas;
    private final com.ccam.service.BoletoService boletos;

    public AppAlunoController(AlunoAuthService authService, ExercicioRepository exercicios,
                              CatalogoExercicioRepository catalogo, TreinoRealizadoRepository realizados,
                              MuralRepository mural, FinanceiroRepository financeiro, ClienteRepository clientes,
                              FrequenciaService frequencia, LogService logService,
                              ProdutoRepository produtos, LojaService lojaService,
                              TreinoRealizadoService treinoRealizado, PixService pixService,
                              com.ccam.service.NotificacaoService notificacoes,
                              com.ccam.service.FotoService fotoService,
                              com.ccam.repository.EmpresaRepository empresas,
                              com.ccam.service.BoletoService boletos) {
        this.authService = authService;
        this.exercicios = exercicios;
        this.catalogo = catalogo;
        this.realizados = realizados;
        this.mural = mural;
        this.financeiro = financeiro;
        this.clientes = clientes;
        this.frequencia = frequencia;
        this.logService = logService;
        this.produtos = produtos;
        this.lojaService = lojaService;
        this.treinoRealizado = treinoRealizado;
        this.pixService = pixService;
        this.notificacoes = notificacoes;
        this.fotoService = fotoService;
        this.empresas = empresas;
        this.boletos = boletos;
    }

    @PostMapping("/login")
    public ResponseEntity<AlunoLoginResponse> login(@RequestBody Map<String, String> corpo) {
        return ResponseEntity.ok(authService.login(corpo.get("email"), corpo.get("senha")));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        authService.logout(AuthInterceptor.extrairToken(request));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/esqueci-senha")
    public Map<String, String> esqueciSenha(@RequestBody Map<String, String> corpo) {
        authService.pedirNovaSenha(corpo.get("email"));
        return Map.of("mensagem", "Se o e-mail estiver cadastrado, a recepção da academia foi avisada.");
    }

    @GetMapping("/foto")
    public ResponseEntity<Map<String, String>> foto(@RequestAttribute(AlunoInterceptor.ATRIBUTO) Cliente aluno) {
        return fotoService.buscar(aluno).map(ResponseEntity::ok).orElse(ResponseEntity.noContent().build());
    }

    @PutMapping("/foto")
    public AlunoPerfil salvarFoto(@RequestAttribute(AlunoInterceptor.ATRIBUTO) Cliente aluno, @RequestBody Map<String, String> corpo) {
        return authService.perfil(fotoService.salvar(aluno, corpo.get("tipo"), corpo.get("base64")));
    }

    @DeleteMapping("/foto")
    public AlunoPerfil removerFoto(@RequestAttribute(AlunoInterceptor.ATRIBUTO) Cliente aluno) {
        return authService.perfil(fotoService.remover(aluno));
    }

    @PutMapping("/senha")
    public ResponseEntity<Void> trocarSenha(@RequestAttribute(AlunoInterceptor.ATRIBUTO) Cliente aluno,
                                            @RequestBody Map<String, String> corpo) {
        authService.trocarSenha(aluno, corpo.get("atual"), corpo.get("nova"));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/notificacoes")
    public List<com.ccam.dto.NotificacaoApp> notificacoes(@RequestAttribute(AlunoInterceptor.ATRIBUTO) Cliente aluno) {
        return notificacoes.doAluno(aluno);
    }

    @GetMapping("/me")
    public AlunoPerfil eu(@RequestAttribute(AlunoInterceptor.ATRIBUTO) Cliente aluno) {
        return authService.perfil(aluno);
    }

    @GetMapping("/treino")
    public List<Exercicio> ficha(@RequestAttribute(AlunoInterceptor.ATRIBUTO) Cliente aluno) {
        return exercicios.findByClienteId(aluno.getId());
    }

    @GetMapping("/catalogo")
    public List<CatalogoExercicio> catalogo() {
        return catalogo.findAll(Sort.by("grupoMuscular", "nome"));
    }

    @PostMapping("/treino")
    @Transactional
    public Exercicio adicionar(@RequestAttribute(AlunoInterceptor.ATRIBUTO) Cliente aluno,
                               @RequestBody AlunoExercicioRequest req) {
        Exercicio ex = new Exercicio();
        ex.setClienteId(aluno.getId());
        if (req.catalogoId() != null) {
            CatalogoExercicio item = catalogo.findById(req.catalogoId())
                    .orElseThrow(() -> new NegocioException("Exercício não encontrado."));
            ex.setNomeExercicio(item.getNome());
            ex.setGrupoMuscular(item.getGrupoMuscular());
            ex.setImagemUrl(item.getImagemUrl());
        } else {
            String nome = limpo(req.nome());
            if (nome == null) {
                throw new NegocioException("Escolha um exercício do catálogo ou digite o nome.");
            }
            ex.setNomeExercicio(nome.length() > 120 ? nome.substring(0, 120) : nome);
            String grupo = limpo(req.grupoMuscular());
            ex.setGrupoMuscular(grupo == null ? "Outros" : (grupo.length() > 60 ? grupo.substring(0, 60) : grupo));
        }
        aplicar(ex, req);
        Exercicio salvo = exercicios.save(ex);
        logService.registrarPeloAluno(aluno, LogService.TREINO, salvo.getId(), LogService.CRIACAO,
                "Exercício adicionado pelo aluno: " + resumo(salvo));
        return salvo;
    }

    @DeleteMapping("/treino")
    @Transactional
    public ResponseEntity<Void> limparFicha(@RequestAttribute(AlunoInterceptor.ATRIBUTO) Cliente aluno) {
        List<Exercicio> ficha = exercicios.findByClienteId(aluno.getId());
        if (!ficha.isEmpty()) {
            exercicios.deleteAll(ficha);
            logService.registrarPeloAluno(aluno, LogService.TREINO, null, LogService.EXCLUSAO,
                    "Aluno apagou a ficha inteira para montar a própria (" + ficha.size() + " exercícios): "
                            + ficha.stream().map(Exercicio::getNomeExercicio).collect(Collectors.joining(", ")));
        }
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/treino/{id}")
    @Transactional
    public Exercicio editar(@RequestAttribute(AlunoInterceptor.ATRIBUTO) Cliente aluno,
                            @PathVariable UUID id, @RequestBody AlunoExercicioRequest req) {
        Exercicio ex = doAluno(aluno, id);
        LogService.Diferencas dif = new LogService.Diferencas()
                .campo("Dia", ex.getDiaSemana(), req.diaSemana())
                .campo("Séries", ex.getSeries(), limpo(req.series()))
                .campo("Repetições", ex.getRepeticoes(), limpo(req.repeticoes()))
                .campo("Carga", ex.getCarga(), limpo(req.carga()));
        aplicar(ex, req);
        if (dif.vazio()) {
            return ex;
        }
        Exercicio salvo = exercicios.save(ex);
        logService.registrarPeloAluno(aluno, LogService.TREINO, salvo.getId(), LogService.EDICAO,
                salvo.getNomeExercicio() + "\n" + dif);
        return salvo;
    }

    @DeleteMapping("/treino/{id}")
    @Transactional
    public ResponseEntity<Void> remover(@RequestAttribute(AlunoInterceptor.ATRIBUTO) Cliente aluno, @PathVariable UUID id) {
        Exercicio ex = doAluno(aluno, id);
        exercicios.delete(ex);
        logService.registrarPeloAluno(aluno, LogService.TREINO, ex.getId(), LogService.EXCLUSAO,
                "Exercício removido pelo aluno: " + resumo(ex));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/treinos-realizados")
    public List<TreinoRealizadoResponse> realizados(@RequestAttribute(AlunoInterceptor.ATRIBUTO) Cliente aluno,
                                                    @RequestParam LocalDate de, @RequestParam LocalDate ate) {
        if (ate.isBefore(de) || ChronoUnit.DAYS.between(de, ate) > 62) {
            throw new NegocioException("Período inválido.");
        }
        return treinoRealizado.listar(aluno, de, ate);
    }

    @PutMapping("/treinos-realizados/{data}")
    public ResponseEntity<TreinoRealizadoResponse> marcar(@RequestAttribute(AlunoInterceptor.ATRIBUTO) Cliente aluno,
                                                          @PathVariable LocalDate data,
                                                          @RequestBody TreinoRealizadoRequest req) {
        return treinoRealizado.marcarCompletos(aluno, data, req.idsFeitos(), req.observacao())
                .map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PutMapping("/treinos-realizados/{data}/exercicios/{exercicioId}")
    public ResponseEntity<TreinoRealizadoResponse> registrar(@RequestAttribute(AlunoInterceptor.ATRIBUTO) Cliente aluno,
                                                             @PathVariable LocalDate data, @PathVariable UUID exercicioId,
                                                             @RequestBody RegistroRequest req) {
        return treinoRealizado.registrar(aluno, data, exercicioId, req.reps(), req.carga())
                .map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/frequencia")
    public FrequenciaResponse frequencia(@RequestAttribute(AlunoInterceptor.ATRIBUTO) Cliente aluno) {
        return frequencia.calcular(aluno);
    }

    @GetMapping("/mural")
    public PaginaResposta<Mural> mural(@RequestAttribute(AlunoInterceptor.ATRIBUTO) Cliente aluno,
                                       @RequestParam(required = false) Integer pagina,
                                       @RequestParam(required = false) Integer tamanho) {
        var page = mural.buscar(aluno.getEmpresaId(), Paginacao.DATA_MINIMA.atStartOfDay(),
                Paginacao.DATA_MAXIMA.atTime(LocalTime.MAX), "",
                Paginacao.criar(pagina, tamanho, Sort.by(Sort.Direction.DESC, "dataPublicacao")));
        return PaginaResposta.de(page);
    }

    @GetMapping("/loja")
    public LojaAppResponse loja(@RequestAttribute(AlunoInterceptor.ATRIBUTO) Cliente aluno) {
        ConfigLoja config = lojaService.config(aluno.getEmpresaId());
        if (!config.isAtiva()) {
            return new LojaAppResponse(false, 0, false, List.of());
        }
        boolean aniversario = aniversarioHoje(aluno, LocalDate.now());
        int desconto = config.getDescontoAniversario();
        List<ProdutoApp> lista = produtos.findByEmpresaIdAndAtivoTrueOrderByCategoriaAscNomeAsc(aluno.getEmpresaId()).stream()
                .map(p -> new ProdutoApp(p.getId(), p.getNome(), p.getDescricao(), p.getCategoria(), p.getPreco(),
                        aniversario && p.isDescontoAniversario() && desconto > 0 ? LojaService.comDesconto(p.getPreco(), desconto) : p.getPreco(),
                        p.isDescontoAniversario()))
                .toList();
        return new LojaAppResponse(true, desconto, aniversario, lista);
    }

    @GetMapping("/financeiro")
    public List<FinanceiroResponse> financeiro(@RequestAttribute(AlunoInterceptor.ATRIBUTO) Cliente aluno) {
        return financeiro.findByClienteIdOrderByVencimentoDesc(aluno.getId()).stream()
                .map(f -> FinanceiroResponse.de(f, aluno)).toList();
    }

    @GetMapping("/financeiro/config")
    public Map<String, Object> configFinanceiro(@RequestAttribute(AlunoInterceptor.ATRIBUTO) Cliente aluno) {
        var config = pixService.registro(aluno.getEmpresaId()).orElse(null);
        Map<String, Object> r = new java.util.HashMap<>();
        r.put("pixAtivo", config != null && config.getChave() != null && !config.getChave().isBlank());
        r.put("diaPixDisponivel", config == null ? null : config.getDiaDisponivel());
        r.put("boletoAtivo", boletos.disponivel());
        return r;
    }

    @GetMapping("/financeiro/{id}/pix")
    public Map<String, Object> pixDaMensalidade(@RequestAttribute(AlunoInterceptor.ATRIBUTO) Cliente aluno, @PathVariable UUID id) {
        Financeiro f = mensalidadeDoAluno(aluno, id);
        if ("Pago".equals(f.getStatus())) {
            throw new NegocioException("Esta mensalidade já está paga.");
        }
        ConfigPix config = pixService.config(aluno.getEmpresaId())
                .orElseThrow(() -> new NegocioException("A academia ainda não cadastrou a chave Pix. Pague na recepção."));
        String txid = "CCAM" + f.getId().toString().replace("-", "").substring(0, 12);
        return Map.of(
                "copiaECola", pixService.copiaECola(config, f.getValor(), txid),
                "valor", f.getValor(),
                "recebedor", config.getNomeRecebedor(),
                "chave", config.getChave());
    }

    @PostMapping("/financeiro/{id}/boleto")
    @Transactional
    public Map<String, Object> boleto(@RequestAttribute(AlunoInterceptor.ATRIBUTO) Cliente aluno, @PathVariable UUID id) {
        Financeiro f = mensalidadeDoAluno(aluno, id);
        if ("Pago".equals(f.getStatus())) {
            throw new NegocioException("Esta mensalidade já está paga.");
        }
        f = boletos.gerar(aluno, f);
        Map<String, Object> r = new java.util.HashMap<>();
        r.put("url", f.getBoletoUrl());
        r.put("linhaDigitavel", f.getLinhaDigitavel());
        r.put("valor", f.getValor());
        r.put("vencimento", f.getVencimento());
        return r;
    }

    @GetMapping("/financeiro/{id}/comprovante")
    public Map<String, Object> comprovante(@RequestAttribute(AlunoInterceptor.ATRIBUTO) Cliente aluno, @PathVariable UUID id) {
        Financeiro f = mensalidadeDoAluno(aluno, id);
        if (!"Pago".equals(f.getStatus())) {
            throw new NegocioException("O comprovante fica disponível depois que a academia confirma o pagamento.");
        }
        var empresa = aluno.getEmpresaId() == null ? null : empresas.findById(aluno.getEmpresaId()).orElse(null);
        String cpf = aluno.getCpf() == null ? "" : aluno.getCpf().replaceAll("[^0-9]", "");
        Map<String, Object> r = new java.util.LinkedHashMap<>();
        r.put("numero", f.getId().toString().replace("-", "").substring(0, 10).toUpperCase());
        r.put("aluno", aluno.getNome());
        r.put("cpf", cpf.length() == 11 ? "***." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-**" : "");
        r.put("academia", empresa == null ? "" : (empresa.getNomeFantasia() != null && !empresa.getNomeFantasia().isBlank()
                ? empresa.getNomeFantasia() : empresa.getRazaoSocial()));
        r.put("razaoSocial", empresa == null ? "" : empresa.getRazaoSocial());
        r.put("cnpj", empresa == null ? "" : empresa.getCnpj());
        r.put("valor", f.getValor());
        r.put("vencimento", f.getVencimento());
        r.put("dataPagamento", f.getDataPagamento());
        r.put("emitidoEm", LocalDateTime.now());
        return r;
    }

    @PostMapping("/financeiro/{id}/informar-pagamento")
    @Transactional
    public FinanceiroResponse informarPagamento(@RequestAttribute(AlunoInterceptor.ATRIBUTO) Cliente aluno, @PathVariable UUID id) {
        Financeiro f = mensalidadeDoAluno(aluno, id);
        if ("Pago".equals(f.getStatus())) {
            throw new NegocioException("Esta mensalidade já está paga.");
        }
        if (f.getPagamentoInformadoEm() == null) {
            f.setPagamentoInformadoEm(LocalDateTime.now());
            f = financeiro.save(f);
            logService.registrarPeloAluno(aluno, LogService.FINANCEIRO, f.getId(), LogService.EDICAO,
                    String.format(java.util.Locale.forLanguageTag("pt-BR"), "Aluno informou pagamento por Pix de R$ %,.2f (vencimento %s). Aguardando confirmação.",
                            f.getValor(), f.getVencimento().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
        }
        return FinanceiroResponse.de(f, aluno);
    }

    private Financeiro mensalidadeDoAluno(Cliente aluno, UUID id) {
        return financeiro.findById(id)
                .filter(f -> aluno.getId().equals(f.getClienteId()))
                .orElseThrow(() -> new NegocioException("Mensalidade não encontrada."));
    }

    private static boolean aniversarioHoje(Cliente c, LocalDate hoje) {
        return c.getDataNascimento() != null
                && c.getDataNascimento().getMonthValue() == hoje.getMonthValue()
                && c.getDataNascimento().getDayOfMonth() == hoje.getDayOfMonth();
    }

    private Exercicio doAluno(Cliente aluno, UUID id) {
        return exercicios.findById(id)
                .filter(e -> aluno.getId().equals(e.getClienteId()))
                .orElseThrow(() -> new NegocioException("Exercício não encontrado."));
    }

    private void aplicar(Exercicio ex, AlunoExercicioRequest req) {
        if (req.diaSemana() == null || !DiasSemana.valido(req.diaSemana())) {
            throw new NegocioException("Escolha o dia da semana.");
        }
        if (limpo(req.series()) == null || limpo(req.repeticoes()) == null) {
            throw new NegocioException("Informe séries e repetições.");
        }
        ex.setDiaSemana(req.diaSemana());
        ex.setSeries(limpo(req.series()));
        ex.setRepeticoes(limpo(req.repeticoes()));
        ex.setCarga(limpo(req.carga()));
    }

    private static String limpo(String v) {
        if (v == null) {
            return null;
        }
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }

    private static String nomeCurto(String nome) {
        String[] partes = nome.trim().split("\\s+");
        if (partes.length == 1) {
            return partes[0];
        }
        return partes[0] + " " + partes[partes.length - 1].charAt(0) + ".";
    }

    private static String resumo(Exercicio ex) {
        return ex.getNomeExercicio() + " (" + ex.getDiaSemana() + ", " + ex.getSeries() + " x " + ex.getRepeticoes()
                + (ex.getCarga() != null ? ", " + ex.getCarga() : "") + ")";
    }
}
