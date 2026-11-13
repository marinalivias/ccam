package com.ccam.ccamv2.controller;

import com.ccam.ccamv2.dto.*;
import com.ccam.ccamv2.exception.NegocioException;
import com.ccam.ccamv2.model.Empresa;
import com.ccam.ccamv2.model.Usuario;
import com.ccam.ccamv2.security.AuthInterceptor;
import com.ccam.ccamv2.security.Permissao;
import com.ccam.ccamv2.security.RequerPermissao;
import com.ccam.ccamv2.service.EmpresaService;
import com.ccam.ccamv2.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class EmpresaController {
    private final EmpresaService service;
    private final UsuarioService usuarioService;

    public EmpresaController(EmpresaService service, UsuarioService usuarioService) {
        this.service = service;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/empresas")
    @RequerPermissao(Permissao.EMPRESAS_GERENCIAR)
    public ResponseEntity<List<EmpresaItem>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @PostMapping("/empresas")
    @RequerPermissao(Permissao.EMPRESAS_GERENCIAR)
    public ResponseEntity<EmpresaItem> cadastrar(@RequestBody NovaEmpresaRequest req) {
        return ResponseEntity.ok(service.criar(req));
    }

    @PutMapping("/empresas/{id}")
    @RequerPermissao(Permissao.EMPRESAS_GERENCIAR)
    public ResponseEntity<EmpresaItem> atualizar(@PathVariable UUID id, @RequestBody EmpresaDados dados) {
        return ResponseEntity.ok(service.atualizar(id, dados));
    }

    @PatchMapping("/empresas/{id}/ativo")
    @RequerPermissao(Permissao.EMPRESAS_GERENCIAR)
    public ResponseEntity<EmpresaItem> alterarAtivo(@PathVariable UUID id, @RequestBody AtivoRequest req) {
        if (req.ativo() == null) {
            throw new NegocioException("Informe se a empresa deve ficar ativa ou inativa.");
        }
        return ResponseEntity.ok(service.alterarAtivo(id, req.ativo()));
    }

    @PostMapping("/empresas/{id}/redefinir-senha-titular")
    @RequerPermissao(Permissao.EMPRESAS_GERENCIAR)
    public ResponseEntity<Map<String, String>> redefinirSenhaTitular(@PathVariable UUID id, @RequestBody SenhaRequest req) {
        service.buscar(id);
        usuarioService.redefinirSenhaTitular(id, req.senha());
        return ResponseEntity.ok(Map.of("mensagem", "Senha do titular redefinida."));
    }

    @GetMapping("/empresa/minha")
    @RequerPermissao(Permissao.EMPRESA_VER)
    public ResponseEntity<Empresa> minhaEmpresa(@RequestAttribute(AuthInterceptor.ATRIBUTO) Usuario usuario) {
        return ResponseEntity.ok(service.buscar(usuario.getEmpresaId()));
    }

    @PutMapping("/empresa/minha")
    @RequerPermissao(Permissao.EMPRESA_EDITAR)
    public ResponseEntity<Empresa> atualizarMinhaEmpresa(
            @RequestAttribute(AuthInterceptor.ATRIBUTO) Usuario usuario,
            @RequestBody EmpresaDados dados) {
        return ResponseEntity.ok(service.atualizarMinha(usuario.getEmpresaId(), dados));
    }
}
