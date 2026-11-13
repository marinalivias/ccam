package com.ccam.ccamv2.controller;

import com.ccam.ccamv2.dto.FinanceiroResponse;
import com.ccam.ccamv2.dto.PaginaResposta;
import com.ccam.ccamv2.exception.NegocioException;
import com.ccam.ccamv2.model.Cliente;
import com.ccam.ccamv2.model.Financeiro;
import com.ccam.ccamv2.model.Usuario;
import com.ccam.ccamv2.repository.ClienteRepository;
import com.ccam.ccamv2.repository.FinanceiroRepository;
import com.ccam.ccamv2.security.AuthInterceptor;
import com.ccam.ccamv2.security.Permissao;
import com.ccam.ccamv2.security.RequerPermissao;
import com.ccam.ccamv2.service.ClienteService;
import com.ccam.ccamv2.service.LogService;
import com.ccam.ccamv2.service.Paginacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/financeiro")
@CrossOrigin(origins = "*")
public class FinanceiroController {
    private static final Set<String> STATUS = Set.of("Pago", "Pendente");
    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final FinanceiroRepository repository;
    private final ClienteRepository clienteRepository;
    private final ClienteService clienteService;
    private final LogService logService;

    public FinanceiroController(FinanceiroRepository repository, ClienteRepository clienteRepository,
                                ClienteService clienteService, LogService logService) {
        this.repository = repository;
        this.clienteRepository = clienteRepository;
        this.clienteService = clienteService;
        this.logService = logService;
    }

    @GetMapping
    @RequerPermissao(Permissao.FINANCEIRO_VER)
    public ResponseEntity<PaginaResposta<FinanceiroResponse>> listar(
            @RequestAttribute(AuthInterceptor.ATRIBUTO) Usuario usuario,
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String mes,
            @RequestParam(required = false) Integer pagina,
            @RequestParam(required = false) Integer tamanho) {
        String s = status != null && STATUS.contains(status) ? status : "";
        YearMonth m = Paginacao.mes(mes);
        LocalDate inicio = m != null ? m.atDay(1) : Paginacao.DATA_MINIMA;
        LocalDate fim = m != null ? m.atEndOfMonth() : Paginacao.DATA_MAXIMA;

        Page<Financeiro> page = repository.buscar(usuario.getEmpresaId(), s, inicio, fim,
                Paginacao.like(Paginacao.termo(busca)),
                Paginacao.criar(pagina, tamanho, Sort.by(Sort.Direction.DESC, "vencimento").and(Sort.by("id"))));
        return ResponseEntity.ok(PaginaResposta.de(page, comNomes(page.getContent())));
    }

    @GetMapping("/cliente/{clienteId}")
    @RequerPermissao(Permissao.FINANCEIRO_VER)
    public ResponseEntity<List<FinanceiroResponse>> buscarPorCliente(@RequestAttribute(AuthInterceptor.ATRIBUTO) Usuario usuario,
                                                                     @PathVariable UUID clienteId) {
        Cliente aluno = aluno(usuario, clienteId);
        return ResponseEntity.ok(repository.findByClienteIdOrderByVencimentoDesc(clienteId).stream()
                .map(f -> FinanceiroResponse.de(f, aluno)).toList());
    }

    @PostMapping
    @RequerPermissao(Permissao.FINANCEIRO_LANCAR)
    @Transactional
    public ResponseEntity<FinanceiroResponse> registrarCobranca(@RequestAttribute(AuthInterceptor.ATRIBUTO) Usuario usuario,
                                                                @RequestBody Financeiro financeiro) {
        Cliente aluno = aluno(usuario, financeiro.getClienteId());
        validar(financeiro);
        financeiro.setId(null);
        financeiro.setEmpresaId(usuario.getEmpresaId());
        if (financeiro.getStatus() == null || financeiro.getStatus().isBlank()) {
            financeiro.setStatus("Pendente");
        }
        Financeiro salvo = repository.save(financeiro);
        logService.registrar(usuario, LogService.FINANCEIRO, salvo.getId(), salvo.getClienteId(), LogService.CRIACAO,
                "Mensalidade lançada: " + resumo(salvo));
        return ResponseEntity.ok(FinanceiroResponse.de(salvo, aluno));
    }

    @PatchMapping("/{id}/pagar")
    @RequerPermissao(Permissao.FINANCEIRO_LANCAR)
    @Transactional
    public ResponseEntity<FinanceiroResponse> marcarComoPago(@RequestAttribute(AuthInterceptor.ATRIBUTO) Usuario usuario,
                                                             @PathVariable UUID id) {
        Financeiro f = daEmpresa(usuario, id);
        if (!"Pago".equals(f.getStatus())) {
            f.setStatus("Pago");
            f = repository.save(f);
            logService.registrar(usuario, LogService.FINANCEIRO, f.getId(), f.getClienteId(), LogService.EDICAO,
                    "Pagamento registrado: " + resumo(f));
        }
        return ResponseEntity.ok(FinanceiroResponse.de(f, clienteRepository.findById(f.getClienteId()).orElse(null)));
    }

    @PutMapping("/{id}")
    @RequerPermissao(Permissao.FINANCEIRO_EDITAR)
    @Transactional
    public ResponseEntity<FinanceiroResponse> editar(@RequestAttribute(AuthInterceptor.ATRIBUTO) Usuario usuario,
                                                     @PathVariable UUID id,
                                                     @RequestBody Financeiro dados) {
        Financeiro atual = daEmpresa(usuario, id);
        validar(dados);
        if (dados.getStatus() == null || !STATUS.contains(dados.getStatus())) {
            throw new NegocioException("Status inválido. Use Pago ou Pendente.");
        }

        LogService.Diferencas dif = new LogService.Diferencas()
                .campo("Valor", moeda(atual.getValor()), moeda(dados.getValor()))
                .campo("Vencimento", atual.getVencimento().format(DATA_BR), dados.getVencimento().format(DATA_BR))
                .campo("Status", atual.getStatus(), dados.getStatus());
        if (!dif.vazio()) {
            atual.setValor(dados.getValor());
            atual.setVencimento(dados.getVencimento());
            atual.setStatus(dados.getStatus());
            atual = repository.save(atual);
            logService.registrar(usuario, LogService.FINANCEIRO, atual.getId(), atual.getClienteId(), LogService.EDICAO,
                    "Mensalidade alterada\n" + dif);
        }
        return ResponseEntity.ok(FinanceiroResponse.de(atual, clienteRepository.findById(atual.getClienteId()).orElse(null)));
    }

    @DeleteMapping("/{id}")
    @RequerPermissao(Permissao.FINANCEIRO_EDITAR)
    @Transactional
    public ResponseEntity<Map<String, String>> excluir(@RequestAttribute(AuthInterceptor.ATRIBUTO) Usuario usuario,
                                                       @PathVariable UUID id) {
        Financeiro atual = daEmpresa(usuario, id);
        repository.delete(atual);
        logService.registrar(usuario, LogService.FINANCEIRO, atual.getId(), atual.getClienteId(), LogService.EXCLUSAO,
                "Mensalidade excluída: " + resumo(atual));
        return ResponseEntity.ok(Map.of("mensagem", "Lançamento excluído."));
    }

    private Function<Financeiro, FinanceiroResponse> comNomes(List<Financeiro> lista) {
        Set<UUID> ids = lista.stream().map(Financeiro::getClienteId).collect(Collectors.toSet());
        Map<UUID, Cliente> alunos = clienteRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Cliente::getId, Function.identity()));
        return f -> FinanceiroResponse.de(f, alunos.get(f.getClienteId()));
    }

    private Cliente aluno(Usuario usuario, UUID clienteId) {
        return clienteService.buscarDaEmpresa(clienteId, usuario.getEmpresaId())
                .orElseThrow(() -> new NegocioException("Aluno não encontrado."));
    }

    private Financeiro daEmpresa(Usuario usuario, UUID id) {
        return repository.findById(id)
                .filter(f -> usuario.getEmpresaId() != null && usuario.getEmpresaId().equals(f.getEmpresaId()))
                .orElseThrow(() -> new NegocioException("Lançamento não encontrado."));
    }

    private void validar(Financeiro f) {
        if (f.getValor() == null || f.getValor() <= 0) {
            throw new NegocioException("Informe um valor maior que zero.");
        }
        if (f.getVencimento() == null) {
            throw new NegocioException("Informe a data de vencimento.");
        }
    }

    private static String moeda(Double valor) {
        return valor == null ? null : String.format(Locale.forLanguageTag("pt-BR"), "R$ %,.2f", valor);
    }

    private static String resumo(Financeiro f) {
        return moeda(f.getValor()) + ", vencimento " + f.getVencimento().format(DATA_BR) + ", " + f.getStatus();
    }
}
