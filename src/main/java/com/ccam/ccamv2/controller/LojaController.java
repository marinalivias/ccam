package com.ccam.ccamv2.controller;

import com.ccam.ccamv2.dto.ConfigLojaRequest;
import com.ccam.ccamv2.dto.PaginaResposta;
import com.ccam.ccamv2.exception.NegocioException;
import com.ccam.ccamv2.model.ConfigLoja;
import com.ccam.ccamv2.model.Produto;
import com.ccam.ccamv2.model.Usuario;
import com.ccam.ccamv2.repository.ProdutoRepository;
import com.ccam.ccamv2.security.AuthInterceptor;
import com.ccam.ccamv2.security.Permissao;
import com.ccam.ccamv2.security.RequerPermissao;
import com.ccam.ccamv2.service.LogService;
import com.ccam.ccamv2.service.LojaService;
import com.ccam.ccamv2.service.Paginacao;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/loja")
@CrossOrigin(origins = "*")
@RequerPermissao(Permissao.LOJA_GERENCIAR)
public class LojaController {
    private static final Set<String> STATUS = Set.of("todos", "visiveis", "ocultos");

    private final ProdutoRepository produtos;
    private final LojaService lojaService;
    private final LogService logService;

    public LojaController(ProdutoRepository produtos, LojaService lojaService, LogService logService) {
        this.produtos = produtos;
        this.lojaService = lojaService;
        this.logService = logService;
    }

    @GetMapping("/config")
    public ConfigLoja config(@RequestAttribute(AuthInterceptor.ATRIBUTO) Usuario usuario) {
        return lojaService.config(usuario.getEmpresaId());
    }

    @PutMapping("/config")
    public ConfigLoja salvarConfig(@RequestAttribute(AuthInterceptor.ATRIBUTO) Usuario usuario, @RequestBody ConfigLojaRequest req) {
        ConfigLoja c = lojaService.config(usuario.getEmpresaId());
        if (req.ativa() != null) {
            c.setAtiva(req.ativa());
        }
        if (req.descontoAniversario() != null) {
            if (req.descontoAniversario() < 0 || req.descontoAniversario() > 90) {
                throw new NegocioException("O desconto de aniversário deve ficar entre 0% e 90%.");
            }
            c.setDescontoAniversario(req.descontoAniversario());
        }
        return lojaService.salvar(c);
    }

    @GetMapping("/produtos")
    public PaginaResposta<Produto> listar(@RequestAttribute(AuthInterceptor.ATRIBUTO) Usuario usuario,
                                          @RequestParam(required = false) String busca,
                                          @RequestParam(defaultValue = "todos") String status,
                                          @RequestParam(required = false) Integer pagina,
                                          @RequestParam(required = false) Integer tamanho) {
        String s = STATUS.contains(status) ? status : "todos";
        var page = produtos.buscar(usuario.getEmpresaId(), s, Paginacao.like(Paginacao.termo(busca)),
                Paginacao.criar(pagina, tamanho, Sort.by("categoria", "nome")));
        return PaginaResposta.de(page);
    }

    @PostMapping("/produtos")
    @Transactional
    public Produto criar(@RequestAttribute(AuthInterceptor.ATRIBUTO) Usuario usuario, @RequestBody Produto dados) {
        Produto p = new Produto();
        p.setEmpresaId(usuario.getEmpresaId());
        aplicar(p, dados);
        Produto salvo = produtos.save(p);
        logService.registrar(usuario, LogService.PRODUTO, salvo.getId(), null, LogService.CRIACAO,
                "Produto cadastrado: " + salvo.getNome() + " (" + moeda(salvo.getPreco()) + ")");
        return salvo;
    }

    @PutMapping("/produtos/{id}")
    @Transactional
    public Produto editar(@RequestAttribute(AuthInterceptor.ATRIBUTO) Usuario usuario,
                          @PathVariable UUID id, @RequestBody Produto dados) {
        Produto p = daEmpresa(usuario, id);
        LogService.Diferencas dif = new LogService.Diferencas()
                .campo("Nome", p.getNome(), limpo(dados.getNome()))
                .campo("Categoria", p.getCategoria(), limpo(dados.getCategoria()))
                .campo("Preço", moeda(p.getPreco()), moeda(dados.getPreco()))
                .campo("Visível no app", p.isAtivo() ? "Sim" : "Não", dados.isAtivo() ? "Sim" : "Não")
                .campo("Desconto de aniversário", p.isDescontoAniversario() ? "Sim" : "Não", dados.isDescontoAniversario() ? "Sim" : "Não")
                .sensivel("Descrição", p.getDescricao(), limpo(dados.getDescricao()));
        aplicar(p, dados);
        if (dif.vazio()) {
            return p;
        }
        p.setAtualizadoEm(LocalDateTime.now());
        Produto salvo = produtos.save(p);
        logService.registrar(usuario, LogService.PRODUTO, salvo.getId(), null, LogService.EDICAO, salvo.getNome() + "\n" + dif);
        return salvo;
    }

    @DeleteMapping("/produtos/{id}")
    @Transactional
    public ResponseEntity<Map<String, String>> apagar(@RequestAttribute(AuthInterceptor.ATRIBUTO) Usuario usuario, @PathVariable UUID id) {
        Produto p = daEmpresa(usuario, id);
        produtos.delete(p);
        logService.registrar(usuario, LogService.PRODUTO, p.getId(), null, LogService.EXCLUSAO, "Produto apagado: " + p.getNome());
        return ResponseEntity.ok(Map.of("mensagem", "Produto apagado."));
    }

    private Produto daEmpresa(Usuario usuario, UUID id) {
        return produtos.findById(id)
                .filter(p -> p.getEmpresaId().equals(usuario.getEmpresaId()))
                .orElseThrow(() -> new NegocioException("Produto não encontrado."));
    }

    private void aplicar(Produto p, Produto dados) {
        String nome = limpo(dados.getNome());
        if (nome == null) {
            throw new NegocioException("Informe o nome do produto.");
        }
        if (dados.getPreco() == null || dados.getPreco() <= 0) {
            throw new NegocioException("Informe um preço maior que zero.");
        }
        p.setNome(nome.length() > 120 ? nome.substring(0, 120) : nome);
        String desc = limpo(dados.getDescricao());
        p.setDescricao(desc != null && desc.length() > 500 ? desc.substring(0, 500) : desc);
        String cat = limpo(dados.getCategoria());
        p.setCategoria(cat != null && cat.length() > 60 ? cat.substring(0, 60) : cat);
        p.setPreco(Math.round(dados.getPreco() * 100) / 100.0);
        p.setAtivo(dados.isAtivo());
        p.setDescontoAniversario(dados.isDescontoAniversario());
    }

    private static String limpo(String v) {
        if (v == null) {
            return null;
        }
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }

    private static String moeda(Double v) {
        return v == null ? null : String.format(Locale.forLanguageTag("pt-BR"), "R$ %,.2f", v);
    }
}
