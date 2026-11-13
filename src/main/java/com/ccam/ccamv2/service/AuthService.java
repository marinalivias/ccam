package com.ccam.ccamv2.service;

import com.ccam.ccamv2.dto.LoginResponse;
import com.ccam.ccamv2.dto.UsuarioInfo;
import com.ccam.ccamv2.exception.NegocioException;
import com.ccam.ccamv2.model.Empresa;
import com.ccam.ccamv2.model.Sessao;
import com.ccam.ccamv2.model.Usuario;
import com.ccam.ccamv2.repository.EmpresaRepository;
import com.ccam.ccamv2.repository.SessaoRepository;
import com.ccam.ccamv2.repository.UsuarioRepository;
import com.ccam.ccamv2.security.Permissao;
import com.ccam.ccamv2.security.SenhaUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class AuthService {
    private static final int MAX_TENTATIVAS = 5;
    private static final int BLOQUEIO_MINUTOS = 15;
    private static final int RESET_VALIDADE_MINUTOS = 30;
    private static final String MSG_CREDENCIAIS = "E-mail ou senha incorretos.";

    private static final String HASH_FALSO = SenhaUtil.hash("senha-falsa-para-igualar-tempo");

    private final UsuarioRepository usuarios;
    private final EmpresaRepository empresas;
    private final SessaoRepository sessoes;
    private final long sessaoHoras;

    public AuthService(UsuarioRepository usuarios,
                       EmpresaRepository empresas,
                       SessaoRepository sessoes,
                       @Value("${ccam.auth.sessao-horas:12}") long sessaoHoras) {
        this.usuarios = usuarios;
        this.empresas = empresas;
        this.sessoes = sessoes;
        this.sessaoHoras = sessaoHoras;
    }

    @Transactional
    public LoginResponse login(String email, String senha) {
        String emailLimpo = email == null ? "" : email.trim().toLowerCase();
        String senhaDigitada = senha == null ? "" : senha;

        Usuario usuario = usuarios.findByEmailIgnoreCase(emailLimpo).orElse(null);
        if (usuario == null) {
            SenhaUtil.confere(senhaDigitada, HASH_FALSO);
            throw new NegocioException(MSG_CREDENCIAIS);
        }

        LocalDateTime agora = LocalDateTime.now();
        if (usuario.getBloqueadoAte() != null && usuario.getBloqueadoAte().isAfter(agora)) {
            throw new NegocioException("Muitas tentativas incorretas. Aguarde alguns minutos e tente novamente.");
        }

        if (!SenhaUtil.confere(senhaDigitada, usuario.getSenhaHash())) {
            usuario.setTentativasFalhas(usuario.getTentativasFalhas() + 1);
            if (usuario.getTentativasFalhas() >= MAX_TENTATIVAS) {
                usuario.setBloqueadoAte(agora.plusMinutes(BLOQUEIO_MINUTOS));
                usuario.setTentativasFalhas(0);
            }
            usuarios.save(usuario);
            throw new NegocioException(MSG_CREDENCIAIS);
        }

        if (!usuario.isAtivo()) {
            throw new NegocioException("Seu usuário está desativado. Procure o gestor da sua academia.");
        }
        validarEmpresaAtiva(usuario);

        usuario.setTentativasFalhas(0);
        usuario.setBloqueadoAte(null);
        usuario.setUltimoAcesso(agora);
        usuarios.save(usuario);

        sessoes.deleteByExpiraEmBefore(agora);

        String token = SenhaUtil.novoToken();
        Sessao sessao = new Sessao();
        sessao.setTokenHash(SenhaUtil.sha256(token));
        sessao.setUsuarioId(usuario.getId());
        sessao.setExpiraEm(agora.plusHours(sessaoHoras));
        sessoes.save(sessao);

        return new LoginResponse(token, sessao.getExpiraEm(), info(usuario));
    }

    public Optional<Usuario> autenticar(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        Optional<Sessao> sessao = sessoes.findById(SenhaUtil.sha256(token));
        if (sessao.isEmpty() || sessao.get().getExpiraEm().isBefore(LocalDateTime.now())) {
            return Optional.empty();
        }
        Usuario usuario = usuarios.findById(sessao.get().getUsuarioId()).orElse(null);
        if (usuario == null || !usuario.isAtivo()) {
            return Optional.empty();
        }
        if (usuario.getEmpresaId() != null) {
            Empresa empresa = empresas.findById(usuario.getEmpresaId()).orElse(null);
            if (empresa == null || !empresa.isAtivo()) {
                return Optional.empty();
            }
        }
        return Optional.of(usuario);
    }

    public void logout(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        String hash = SenhaUtil.sha256(token);
        if (sessoes.existsById(hash)) {
            sessoes.deleteById(hash);
        }
    }

    @Transactional
    public void alterarSenha(Usuario usuario, String senhaAtual, String novaSenha) {
        if (!SenhaUtil.confere(senhaAtual, usuario.getSenhaHash())) {
            throw new NegocioException("A senha atual está incorreta.");
        }
        UsuarioService.validarSenha(novaSenha);
        usuario.setSenhaHash(SenhaUtil.hash(novaSenha));
        usuarios.save(usuario);
        sessoes.deleteByUsuarioId(usuario.getId());
    }

    @Transactional
    public Optional<String> solicitarRecuperacao(String email) {
        String emailLimpo = email == null ? "" : email.trim().toLowerCase();
        Usuario usuario = usuarios.findByEmailIgnoreCase(emailLimpo).orElse(null);
        if (usuario == null || !usuario.isAtivo()) {
            return Optional.empty();
        }
        if (usuario.getEmpresaId() != null) {
            Empresa empresa = empresas.findById(usuario.getEmpresaId()).orElse(null);
            if (empresa == null || !empresa.isAtivo()) {
                return Optional.empty();
            }
        }
        String token = SenhaUtil.novoToken();
        usuario.setResetTokenHash(SenhaUtil.sha256(token));
        usuario.setResetExpiraEm(LocalDateTime.now().plusMinutes(RESET_VALIDADE_MINUTOS));
        usuarios.save(usuario);
        return Optional.of(token);
    }

    @Transactional
    public void redefinirSenha(String token, String novaSenha) {
        if (token == null || token.isBlank()) {
            throw new NegocioException("Link de recuperação inválido.");
        }
        Usuario usuario = usuarios.findByResetTokenHash(SenhaUtil.sha256(token))
                .orElseThrow(() -> new NegocioException("Link de recuperação inválido ou já utilizado."));
        if (usuario.getResetExpiraEm() == null || usuario.getResetExpiraEm().isBefore(LocalDateTime.now())) {
            throw new NegocioException("Este link de recuperação expirou. Solicite um novo.");
        }
        UsuarioService.validarSenha(novaSenha);

        usuario.setSenhaHash(SenhaUtil.hash(novaSenha));
        usuario.setResetTokenHash(null);
        usuario.setResetExpiraEm(null);
        usuario.setTentativasFalhas(0);
        usuario.setBloqueadoAte(null);
        usuarios.save(usuario);
        sessoes.deleteByUsuarioId(usuario.getId());
    }

    public UsuarioInfo info(Usuario u) {
        String empresaNome = null;
        if (u.getEmpresaId() != null) {
            empresaNome = empresas.findById(u.getEmpresaId())
                    .map(e -> e.getNomeFantasia() != null ? e.getNomeFantasia() : e.getRazaoSocial())
                    .orElse(null);
        }
        List<String> permissoes = u.getPerfil().getPermissoes().stream()
                .sorted(Comparator.comparing(Permissao::ordinal))
                .map(Permissao::name)
                .toList();
        return new UsuarioInfo(u.getId(), u.getNome(), u.getEmail(),
                u.getPerfil().name(), u.getPerfil().getRotulo(), u.isTitular(),
                u.getEmpresaId(), empresaNome, permissoes);
    }

    private void validarEmpresaAtiva(Usuario usuario) {
        if (usuario.getEmpresaId() == null) {
            return;
        }
        Empresa empresa = empresas.findById(usuario.getEmpresaId()).orElse(null);
        if (empresa == null || !empresa.isAtivo()) {
            throw new NegocioException("O acesso desta empresa está suspenso. Fale com o administrador do sistema.");
        }
    }
}
