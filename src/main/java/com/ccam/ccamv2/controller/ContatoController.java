package com.ccam.ccamv2.controller;

import com.ccam.ccamv2.dto.AtivoRequest;
import com.ccam.ccamv2.dto.ContatoRequest;
import com.ccam.ccamv2.dto.PaginaResposta;
import com.ccam.ccamv2.exception.NegocioException;
import com.ccam.ccamv2.model.ContatoInteresse;
import com.ccam.ccamv2.repository.ContatoInteresseRepository;
import com.ccam.ccamv2.security.Permissao;
import com.ccam.ccamv2.security.RequerPermissao;
import com.ccam.ccamv2.service.Paginacao;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/contato")
@CrossOrigin(origins = "*")
public class ContatoController {
    private static final Set<String> STATUS = Set.of("todos", "pendentes", "atendidos");

    private final ContatoInteresseRepository repository;

    public ContatoController(ContatoInteresseRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> enviar(@RequestBody ContatoRequest req) {
        Map<String, String> ok = Map.of("mensagem", "Recebemos o seu contato. Em breve nossa equipe vai falar com você.");
        if (req.site() != null && !req.site().isBlank()) {
            return ResponseEntity.ok(ok);
        }

        String nome = limpo(req.nome(), 120);
        String email = limpo(req.email(), 160);
        if (nome == null) {
            throw new NegocioException("Informe o seu nome.");
        }
        if (email == null || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new NegocioException("Informe um e-mail válido.");
        }

        ContatoInteresse c = new ContatoInteresse();
        c.setNome(nome);
        c.setEmail(email.toLowerCase());
        c.setTelefone(limpo(req.telefone(), 20));
        c.setAcademia(limpo(req.academia(), 160));
        c.setCidade(limpo(req.cidade(), 120));
        c.setMensagem(limpo(req.mensagem(), 2000));
        c.setDataEnvio(LocalDateTime.now());
        repository.save(c);
        return ResponseEntity.ok(ok);
    }

    @GetMapping
    @RequerPermissao(Permissao.EMPRESAS_GERENCIAR)
    public ResponseEntity<PaginaResposta<ContatoInteresse>> listar(
            @RequestParam(required = false) String busca,
            @RequestParam(defaultValue = "pendentes") String status,
            @RequestParam(required = false) Integer pagina,
            @RequestParam(required = false) Integer tamanho) {
        String s = STATUS.contains(status) ? status : "pendentes";
        var page = repository.buscar(s, Paginacao.like(Paginacao.termo(busca)),
                Paginacao.criar(pagina, tamanho, Sort.by(Sort.Direction.DESC, "dataEnvio")));
        return ResponseEntity.ok(PaginaResposta.de(page));
    }

    @PatchMapping("/{id}/atendido")
    @RequerPermissao(Permissao.EMPRESAS_GERENCIAR)
    public ResponseEntity<ContatoInteresse> marcarAtendido(@PathVariable UUID id, @RequestBody AtivoRequest req) {
        ContatoInteresse c = repository.findById(id)
                .orElseThrow(() -> new NegocioException("Contato não encontrado."));
        c.setAtendido(req.ativo() == null || req.ativo());
        return ResponseEntity.ok(repository.save(c));
    }

    private static String limpo(String valor, int max) {
        if (valor == null) {
            return null;
        }
        String t = valor.trim();
        if (t.isEmpty()) {
            return null;
        }
        return t.length() > max ? t.substring(0, max) : t;
    }
}
