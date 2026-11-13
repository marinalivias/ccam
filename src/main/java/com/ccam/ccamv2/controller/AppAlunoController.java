package com.ccam.ccamv2.controller;

import com.ccam.ccamv2.dto.*;
import com.ccam.ccamv2.exception.NegocioException;
import com.ccam.ccamv2.model.*;
import com.ccam.ccamv2.repository.*;
import com.ccam.ccamv2.security.AlunoInterceptor;
import com.ccam.ccamv2.security.AuthInterceptor;
import com.ccam.ccamv2.service.*;
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

    public AppAlunoController(AlunoAuthService authService, ExercicioRepository exercicios,
                              CatalogoExercicioRepository catalogo, TreinoRealizadoRepository realizados,
                              MuralRepository mural, FinanceiroRepository financeiro, ClienteRepository clientes,
                              FrequenciaService frequencia, LogService logService) {
        this.authService = authService;
        this.exercicios = exercicios;
        this.catalogo = catalogo;
        this.realizados = realizados;
        this.mural = mural;
        this.financeiro = financeiro;
        this.clientes = clientes;
        this.frequencia = frequencia;
        this.logService = logService;
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
        if (req.catalogoId() == null) {
            throw new NegocioException("Escolha um exercício.");
        }
        CatalogoExercicio item = catalogo.findById(req.catalogoId())
                .orElseThrow(() -> new NegocioException("Exercício não encontrado."));
        Exercicio ex = new Exercicio();
        ex.setClienteId(aluno.getId());
        ex.setNomeExercicio(item.getNome());
        ex.setGrupoMuscular(item.getGrupoMuscular());
        ex.setImagemUrl(item.getImagemUrl());
        aplicar(ex, req);
        Exercicio salvo = exercicios.save(ex);
        logService.registrarPeloAluno(aluno, LogService.TREINO, salvo.getId(), LogService.CRIACAO,
                "Exercício adicionado pelo aluno: " + resumo(salvo));
        return salvo;
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
        return realizados.findByClienteIdAndDataBetweenOrderByDataAsc(aluno.getId(), de, ate)
                .stream().map(TreinoRealizadoResponse::de).toList();
    }

    @PutMapping("/treinos-realizados/{data}")
    @Transactional
    public ResponseEntity<TreinoRealizadoResponse> marcar(@RequestAttribute(AlunoInterceptor.ATRIBUTO) Cliente aluno,
                                                          @PathVariable LocalDate data,
                                                          @RequestBody TreinoRealizadoRequest req) {
        LocalDate hoje = LocalDate.now();
        if (data.isAfter(hoje)) {
            throw new NegocioException("Não dá para marcar um treino no futuro.");
        }
        if (data.isBefore(hoje.minusDays(30))) {
            throw new NegocioException("Só dá para marcar treinos dos últimos 30 dias.");
        }

        String dia = DiasSemana.de(data);
        List<Exercicio> previstos = exercicios.findByClienteId(aluno.getId()).stream()
                .filter(e -> dia.equals(e.getDiaSemana()) || DiasSemana.ROTATIVO.equals(e.getDiaSemana()))
                .toList();
        Set<UUID> validos = previstos.stream().map(Exercicio::getId).collect(Collectors.toSet());
        List<UUID> feitos = req.idsFeitos() == null ? List.of()
                : req.idsFeitos().stream().filter(validos::contains).distinct().toList();

        Optional<TreinoRealizado> existente = realizados.findByClienteIdAndData(aluno.getId(), data);
        if (feitos.isEmpty()) {
            existente.ifPresent(realizados::delete);
            return ResponseEntity.noContent().build();
        }

        TreinoRealizado t = existente.orElseGet(TreinoRealizado::new);
        t.setEmpresaId(aluno.getEmpresaId());
        t.setClienteId(aluno.getId());
        t.setData(data);
        t.setDiaSemana(dia);
        t.setExerciciosPrevistos(previstos.size());
        t.setExerciciosFeitos(feitos.size());
        t.setIdsFeitos(feitos.stream().map(UUID::toString).collect(Collectors.joining(",")));
        t.setConcluido(feitos.size() >= previstos.size());
        String obs = limpo(req.observacao());
        t.setObservacao(obs != null && obs.length() > 500 ? obs.substring(0, 500) : obs);
        t.setAtualizadoEm(LocalDateTime.now());
        return ResponseEntity.ok(TreinoRealizadoResponse.de(realizados.save(t)));
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

    @GetMapping("/aniversariantes")
    public List<AniversarianteResponse> aniversariantes(@RequestAttribute(AlunoInterceptor.ATRIBUTO) Cliente aluno,
                                                        @RequestParam(required = false) String mes) {
        YearMonth m = Paginacao.mes(mes);
        int numeroMes = (m != null ? m : YearMonth.now()).getMonthValue();
        LocalDate hoje = LocalDate.now();
        return clientes.findByEmpresaIdAndAtivoTrueOrderByNomeAsc(aluno.getEmpresaId()).stream()
                .filter(c -> c.getDataNascimento() != null && c.getDataNascimento().getMonthValue() == numeroMes)
                .sorted(Comparator.comparingInt((Cliente c) -> c.getDataNascimento().getDayOfMonth()).thenComparing(Cliente::getNome))
                .map(c -> new AniversarianteResponse(nomeCurto(c.getNome()), c.getDataNascimento().getDayOfMonth(), numeroMes,
                        c.getDataNascimento().getMonthValue() == hoje.getMonthValue()
                                && c.getDataNascimento().getDayOfMonth() == hoje.getDayOfMonth(),
                        c.getId().equals(aluno.getId())))
                .toList();
    }

    @GetMapping("/financeiro")
    public List<FinanceiroResponse> financeiro(@RequestAttribute(AlunoInterceptor.ATRIBUTO) Cliente aluno) {
        return financeiro.findByClienteIdOrderByVencimentoDesc(aluno.getId()).stream()
                .map(f -> FinanceiroResponse.de(f, aluno)).toList();
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
