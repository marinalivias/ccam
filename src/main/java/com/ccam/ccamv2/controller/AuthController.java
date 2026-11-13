package com.ccam.ccamv2.controller;

import com.ccam.ccamv2.dto.*;
import com.ccam.ccamv2.model.Usuario;
import com.ccam.ccamv2.security.AuthInterceptor;
import com.ccam.ccamv2.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final boolean exibirLinkRecuperacao;

    public AuthController(AuthService authService,
                          @Value("${ccam.auth.exibir-link-reset:false}") boolean exibirLinkRecuperacao) {
        this.authService = authService;
        this.exibirLinkRecuperacao = exibirLinkRecuperacao;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req.email(), req.senha()));
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioInfo> me(@RequestAttribute(AuthInterceptor.ATRIBUTO) Usuario usuario) {
        return ResponseEntity.ok(authService.info(usuario));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        authService.logout(AuthInterceptor.extrairToken(request));
        return ResponseEntity.ok(Map.of("mensagem", "Sessão encerrada."));
    }

    @PostMapping("/alterar-senha")
    public ResponseEntity<Map<String, String>> alterarSenha(
            @RequestAttribute(AuthInterceptor.ATRIBUTO) Usuario usuario,
            @RequestBody AlterarSenhaRequest req) {
        authService.alterarSenha(usuario, req.senhaAtual(), req.novaSenha());
        return ResponseEntity.ok(Map.of("mensagem", "Senha alterada com sucesso."));
    }

    @PostMapping("/esqueci-senha")
    public ResponseEntity<Map<String, String>> esqueciSenha(@RequestBody EsqueciSenhaRequest req) {
        Optional<String> token = authService.solicitarRecuperacao(req.email());

        Map<String, String> resposta = new LinkedHashMap<>();
        resposta.put("mensagem",
                "Se o e-mail informado estiver cadastrado, enviaremos as instruções para redefinir a senha.");

        if (exibirLinkRecuperacao && token.isPresent()) {
            resposta.put("tokenDesenvolvimento", token.get());
            log.info("[DEV] Link de recuperação para {}: /redefinir-senha.html?token={}", req.email(), token.get());
        }
        return ResponseEntity.ok(resposta);
    }

    @PostMapping("/redefinir-senha")
    public ResponseEntity<Map<String, String>> redefinirSenha(@RequestBody RedefinirSenhaRequest req) {
        authService.redefinirSenha(req.token(), req.novaSenha());
        return ResponseEntity.ok(Map.of("mensagem", "Senha redefinida com sucesso. Faça login com a nova senha."));
    }
}
