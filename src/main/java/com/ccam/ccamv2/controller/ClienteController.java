package com.ccam.ccamv2.controller;

import com.ccam.ccamv2.dto.AtivoRequest;
import com.ccam.ccamv2.dto.ClienteDetalhe;
import com.ccam.ccamv2.dto.ClienteResponse;
import com.ccam.ccamv2.dto.LogResponse;
import com.ccam.ccamv2.dto.PaginaResposta;
import com.ccam.ccamv2.exception.NegocioException;
import com.ccam.ccamv2.model.Cliente;
import com.ccam.ccamv2.model.Usuario;
import com.ccam.ccamv2.repository.LogAlteracaoRepository;
import com.ccam.ccamv2.security.AuthInterceptor;
import com.ccam.ccamv2.security.Permissao;
import com.ccam.ccamv2.security.RequerPermissao;
import com.ccam.ccamv2.service.ClienteService;
import com.ccam.ccamv2.service.Paginacao;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "*")
public class ClienteController {
    private final ClienteService service;
    private final LogAlteracaoRepository logs;

    public ClienteController(ClienteService service, LogAlteracaoRepository logs) {
        this.service = service;
        this.logs = logs;
    }

    @PostMapping
    @RequerPermissao(Permissao.ALUNOS_EDITAR)
    public ResponseEntity<?> cadastrar(@RequestAttribute(AuthInterceptor.ATRIBUTO) Usuario usuario,
                                       @RequestBody Cliente cliente) {
        return ResponseEntity.ok(ClienteResponse.de(service.cadastrar(usuario, cliente)));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credenciais) {
        try {
            String email = credenciais.get("email");
            String cpf = credenciais.get("senha");

            Cliente cliente = service.fazerLogin(email, cpf);
            return ResponseEntity.ok(ClienteResponse.de(cliente));
        } catch (RuntimeException e) {
            String mensagem = e.getMessage() != null ? e.getMessage() : "E-mail ou senha (CPF) incorretos.";
            return ResponseEntity.badRequest().body(Map.of("erro", mensagem));
        }
    }

    @GetMapping
    @RequerPermissao({Permissao.ALUNOS_VER, Permissao.TREINOS_VER, Permissao.FINANCEIRO_VER, Permissao.CRM_VER})
    public ResponseEntity<List<ClienteResponse>> listar(@RequestAttribute(AuthInterceptor.ATRIBUTO) Usuario usuario) {
        return ResponseEntity.ok(
                service.listarAtivos(usuario.getEmpresaId()).stream().map(ClienteResponse::de).toList());
    }

    @GetMapping("/busca")
    @RequerPermissao(Permissao.ALUNOS_VER)
    public ResponseEntity<PaginaResposta<ClienteResponse>> buscar(
            @RequestAttribute(AuthInterceptor.ATRIBUTO) Usuario usuario,
            @RequestParam(required = false) String busca,
            @RequestParam(defaultValue = "ativos") String status,
            @RequestParam(required = false) Integer pagina,
            @RequestParam(required = false) Integer tamanho) {
        var page = service.buscar(usuario.getEmpresaId(), busca, status,
                Paginacao.criar(pagina, tamanho, Sort.by("nome").ascending()));
        return ResponseEntity.ok(PaginaResposta.de(page, ClienteResponse::de));
    }

    @GetMapping("/{id}")
    @RequerPermissao(Permissao.ALUNOS_VER)
    public ResponseEntity<ClienteDetalhe> detalhar(@RequestAttribute(AuthInterceptor.ATRIBUTO) Usuario usuario,
                                                   @PathVariable UUID id) {
        return ResponseEntity.ok(ClienteDetalhe.de(encontrar(usuario, id)));
    }

    @PutMapping("/{id}")
    @RequerPermissao(Permissao.ALUNOS_EDITAR)
    public ResponseEntity<ClienteDetalhe> atualizar(@RequestAttribute(AuthInterceptor.ATRIBUTO) Usuario usuario,
                                                    @PathVariable UUID id,
                                                    @RequestBody Cliente dados) {
        return ResponseEntity.ok(ClienteDetalhe.de(service.atualizar(usuario, id, dados)));
    }

    @PatchMapping("/{id}/ativo")
    @RequerPermissao(Permissao.ALUNOS_EDITAR)
    public ResponseEntity<ClienteResponse> alterarAtivo(@RequestAttribute(AuthInterceptor.ATRIBUTO) Usuario usuario,
                                                        @PathVariable UUID id,
                                                        @RequestBody AtivoRequest req) {
        if (req.ativo() == null) {
            throw new NegocioException("Informe se o aluno fica ativo ou inativo.");
        }
        return ResponseEntity.ok(ClienteResponse.de(service.alterarAtivo(usuario, id, req.ativo())));
    }

    @GetMapping("/{id}/historico")
    @RequerPermissao(Permissao.ALUNOS_VER)
    public ResponseEntity<PaginaResposta<LogResponse>> historico(
            @RequestAttribute(AuthInterceptor.ATRIBUTO) Usuario usuario,
            @PathVariable UUID id,
            @RequestParam(required = false) Integer pagina,
            @RequestParam(required = false) Integer tamanho) {
        encontrar(usuario, id);
        var page = logs.findByEmpresaIdAndAlunoId(usuario.getEmpresaId(), id,
                Paginacao.criar(pagina, tamanho, Sort.by(Sort.Direction.DESC, "dataHora")));
        return ResponseEntity.ok(PaginaResposta.de(page, LogResponse::de));
    }

    private Cliente encontrar(Usuario usuario, UUID id) {
        return service.buscarDaEmpresa(id, usuario.getEmpresaId())
                .orElseThrow(() -> new NegocioException("Aluno não encontrado."));
    }
}
