package com.ccam.ccamv2.security;

import com.ccam.ccamv2.model.Cliente;
import com.ccam.ccamv2.service.AlunoAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component
public class AlunoInterceptor implements HandlerInterceptor {
    public static final String ATRIBUTO = "alunoLogado";

    private final AlunoAuthService authService;

    public AlunoInterceptor(AlunoAuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || !(handler instanceof HandlerMethod)) {
            return true;
        }
        if ("POST".equalsIgnoreCase(request.getMethod()) && "/api/app/login".equals(request.getRequestURI())) {
            return true;
        }
        Optional<Cliente> aluno = authService.autenticar(AuthInterceptor.extrairToken(request));
        if (aluno.isEmpty()) {
            response.setStatus(401);
            response.setContentType("application/json");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write("{\"erro\":\"Sua sessão expirou. Entre novamente.\"}");
            return false;
        }
        request.setAttribute(ATRIBUTO, aluno.get());
        return true;
    }
}
