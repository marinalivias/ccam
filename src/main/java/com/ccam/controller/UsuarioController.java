package com.ccam.controller;

import com.ccam.dto.PerfilInfo;
import com.ccam.dto.UsuarioRequest;
import com.ccam.dto.UsuarioResponse;
import com.ccam.model.Usuario;
import com.ccam.security.AuthInterceptor;
import com.ccam.security.Perfil;
import com.ccam.security.Permissao;
import com.ccam.security.RequerPermissao;
import com.ccam.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
@RequerPermissao(Permissao.USUARIOS_GERENCIAR)
public class UsuarioController {
    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listar(@RequestAttribute(AuthInterceptor.ATRIBUTO) Usuario logado) {
        return ResponseEntity.ok(service.listar(logado.getEmpresaId()));
    }

    @GetMapping("/perfis")
    public ResponseEntity<List<PerfilInfo>> perfis() {
        List<PerfilInfo> lista = List.of(Perfil.GESTOR, Perfil.PROFESSOR, Perfil.ATENDENTE).stream()
                .map(p -> new PerfilInfo(p.name(), p.getRotulo(),
                        p.getPermissoes().stream().sorted(Comparator.comparing(Enum::ordinal)).map(Enum::name).toList()))
                .toList();
        return ResponseEntity.ok(lista);
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> criar(
            @RequestAttribute(AuthInterceptor.ATRIBUTO) Usuario logado,
            @RequestBody UsuarioRequest req) {
        return ResponseEntity.ok(service.criar(logado, req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> atualizar(
            @RequestAttribute(AuthInterceptor.ATRIBUTO) Usuario logado,
            @PathVariable UUID id,
            @RequestBody UsuarioRequest req) {
        return ResponseEntity.ok(service.atualizar(logado, id, req));
    }
}
