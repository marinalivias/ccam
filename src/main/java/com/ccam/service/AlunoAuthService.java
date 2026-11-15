package com.ccam.service;

import com.ccam.dto.AlunoLoginResponse;
import com.ccam.dto.AlunoPerfil;
import com.ccam.exception.NegocioException;
import com.ccam.model.Cliente;
import com.ccam.model.Empresa;
import com.ccam.model.SessaoAluno;
import com.ccam.repository.ClienteRepository;
import com.ccam.repository.EmpresaRepository;
import com.ccam.repository.SessaoAlunoRepository;
import com.ccam.security.SenhaUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AlunoAuthService {
    private final ClienteRepository clientes;
    private final EmpresaRepository empresas;
    private final SessaoAlunoRepository sessoes;
    private final long sessaoDias;

    public AlunoAuthService(ClienteRepository clientes,
                            EmpresaRepository empresas,
                            SessaoAlunoRepository sessoes,
                            @Value("${ccam.app.sessao-dias:30}") long sessaoDias) {
        this.clientes = clientes;
        this.empresas = empresas;
        this.sessoes = sessoes;
        this.sessaoDias = sessaoDias;
    }

    @Transactional
    public AlunoLoginResponse login(String email, String senha) {
        Cliente aluno = clientes.findByEmailIgnoreCase(email == null ? "" : email.trim())
                .filter(c -> senhaConfere(c, senha))
                .orElseThrow(() -> new NegocioException("E-mail ou senha incorretos."));
        if (!aluno.isAtivo()) {
            throw new NegocioException("Seu cadastro está inativo. Procure a recepção da academia.");
        }
        if (!empresaAtiva(aluno)) {
            throw new NegocioException("A academia está com o acesso suspenso no momento.");
        }

        sessoes.deleteByExpiraEmBefore(LocalDateTime.now());
        String token = SenhaUtil.novoToken();
        SessaoAluno sessao = new SessaoAluno();
        sessao.setTokenHash(SenhaUtil.sha256(token));
        sessao.setClienteId(aluno.getId());
        sessao.setExpiraEm(LocalDateTime.now().plusDays(sessaoDias));
        sessoes.save(sessao);
        return new AlunoLoginResponse(token, sessao.getExpiraEm(), perfil(aluno));
    }

    @Transactional
    public void trocarSenha(Cliente aluno, String atual, String nova) {
        if (!senhaConfere(aluno, atual)) {
            throw new NegocioException("A senha atual não confere.");
        }
        if (nova == null || nova.length() < 6) {
            throw new NegocioException("A nova senha precisa ter pelo menos 6 caracteres.");
        }
        aluno.setSenhaHash(SenhaUtil.hash(nova));
        clientes.save(aluno);
    }

    private static boolean senhaConfere(Cliente c, String senha) {
        if (senha == null || senha.isEmpty()) {
            return false;
        }
        if (c.getSenhaHash() != null) {
            return SenhaUtil.confere(senha, c.getSenhaHash());
        }
        String digitos = senha.replaceAll("[^0-9]", "");
        return c.getCpf() != null && !digitos.isEmpty() && c.getCpf().replaceAll("[^0-9]", "").equals(digitos);
    }

    public Optional<Cliente> autenticar(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return sessoes.findById(SenhaUtil.sha256(token))
                .filter(s -> s.getExpiraEm().isAfter(LocalDateTime.now()))
                .flatMap(s -> clientes.findById(s.getClienteId()))
                .filter(Cliente::isAtivo)
                .filter(this::empresaAtiva);
    }

    public void logout(String token) {
        if (token != null && !token.isBlank()) {
            sessoes.deleteById(SenhaUtil.sha256(token));
        }
    }

    public AlunoPerfil perfil(Cliente aluno) {
        String academia = aluno.getEmpresaId() == null ? null : empresas.findById(aluno.getEmpresaId())
                .map(e -> e.getNomeFantasia() != null && !e.getNomeFantasia().isBlank() ? e.getNomeFantasia() : e.getRazaoSocial())
                .orElse(null);
        return AlunoPerfil.de(aluno, academia);
    }

    private boolean empresaAtiva(Cliente aluno) {
        return aluno.getEmpresaId() == null || empresas.findById(aluno.getEmpresaId()).map(Empresa::isAtivo).orElse(false);
    }
}
