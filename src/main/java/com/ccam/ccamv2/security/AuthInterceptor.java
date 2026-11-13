package com.ccam.ccamv2.security;

import com.ccam.ccamv2.model.Usuario;
import com.ccam.ccamv2.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    public static final String ATRIBUTO = "usuarioLogado";

    private static final Set<String> POST_PUBLICOS = Set.of(
            "/api/auth/login",
            "/api/auth/esqueci-senha",
            "/api/auth/redefinir-senha",
            "/api/contato"
    );

    private final AuthService authService;

    public AuthInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        if (!(handler instanceof HandlerMethod metodo)) {
            return true;
        }
        if (ehPublica(request)) {
            return true;
        }

        Optional<Usuario> usuario = authService.autenticar(extrairToken(request));
        if (usuario.isEmpty()) {
            responder(response, 401, "Sua sessão expirou. Faça login novamente.");
            return false;
        }

        RequerPermissao exigida = AnnotatedElementUtils.findMergedAnnotation(metodo.getMethod(), RequerPermissao.class);
        if (exigida == null) {
            exigida = AnnotatedElementUtils.findMergedAnnotation(metodo.getBeanType(), RequerPermissao.class);
        }
        if (exigida != null && !possuiAlguma(usuario.get(), exigida.value())) {
            responder(response, 403, "Você não tem permissão para realizar esta ação.");
            return false;
        }

        request.setAttribute(ATRIBUTO, usuario.get());
        return true;
    }

    private boolean ehPublica(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod()) && POST_PUBLICOS.contains(request.getRequestURI());
    }

    private boolean possuiAlguma(Usuario usuario, Permissao[] permissoes) {
        for (Permissao p : permissoes) {
            if (usuario.getPerfil().possui(p)) {
                return true;
            }
        }
        return false;
    }

    public static String extrairToken(HttpServletRequest request) {
        String cabecalho = request.getHeader("Authorization");
        if (cabecalho != null && cabecalho.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return cabecalho.substring(7).trim();
        }
        return null;
    }

    private void responder(HttpServletResponse response, int status, String mensagem) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"erro\":\"" + mensagem + "\"}");
    }
}
