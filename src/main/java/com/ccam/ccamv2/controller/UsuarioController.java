package com.ccam.ccamv2.controller;

import com.ccam.ccamv2.dto.PerfilInfo;
import com.ccam.ccamv2.dto.UsuarioRequest;
import com.ccam.ccamv2.dto.UsuarioResponse;
import com.ccam.ccamv2.model.Usuario;
import com.ccam.ccamv2.security.AuthInterceptor;
import com.ccam.ccamv2.security.Perfil;
import com.ccam.ccamv2.security.Permissao;
import com.ccam.ccamv2.security.RequerPermissao;
import com.ccam.ccamv2.service.UsuarioService;
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
