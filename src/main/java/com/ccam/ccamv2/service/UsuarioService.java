package com.ccam.ccamv2.service;

import com.ccam.ccamv2.dto.TitularDados;
import com.ccam.ccamv2.dto.UsuarioRequest;
import com.ccam.ccamv2.dto.UsuarioResponse;
import com.ccam.ccamv2.exception.NegocioException;
import com.ccam.ccamv2.model.Usuario;
import com.ccam.ccamv2.repository.SessaoRepository;
import com.ccam.ccamv2.repository.UsuarioRepository;
import com.ccam.ccamv2.security.Perfil;
import com.ccam.ccamv2.security.SenhaUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UsuarioService {
    public static final int SENHA_MINIMA = 6;

    private final UsuarioRepository usuarios;
    private final SessaoRepository sessoes;

    public UsuarioService(UsuarioRepository usuarios, SessaoRepository sessoes) {
        this.usuarios = usuarios;
        this.sessoes = sessoes;
    }

    public static void validarSenha(String senha) {
        if (senha == null || senha.length() < SENHA_MINIMA) {
            throw new NegocioException("A senha precisa ter pelo menos " + SENHA_MINIMA + " caracteres.");
        }
        if (senha.length() > 72) {
            throw new NegocioException("A senha pode ter no máximo 72 caracteres.");
        }
    }

    public List<UsuarioResponse> listar(UUID empresaId) {
        return usuarios.findByEmpresaIdOrderByNomeAsc(empresaId).stream().map(UsuarioResponse::de).toList();
    }

    @Transactional
    public Usuario criarTitular(UUID empresaId, TitularDados dados) {
        return novoUsuario(empresaId, dados.nome(), dados.email(), dados.senha(), Perfil.GESTOR, true);
    }

    @Transactional
    public UsuarioResponse criar(Usuario logado, UsuarioRequest req) {
        Perfil perfil = perfilDeEmpresa(req.perfil());
        Usuario novo = novoUsuario(logado.getEmpresaId(), req.nome(), req.email(), req.senha(), perfil, false);
        return UsuarioResponse.de(novo);
    }

    @Transactional
    public UsuarioResponse atualizar(Usuario logado, UUID id, UsuarioRequest req) {
        Usuario alvo = usuarios.findById(id)
                .filter(u -> logado.getEmpresaId() != null && logado.getEmpresaId().equals(u.getEmpresaId()))
                .orElseThrow(() -> new NegocioException("Usuário não encontrado."));
        boolean eleMesmo = alvo.getId().equals(logado.getId());

        String nome = Texto.limpo(req.nome());
        if (nome != null) {
            alvo.setNome(nome);
        }

        if (req.perfil() != null) {
            Perfil novoPerfil = perfilDeEmpresa(req.perfil());
            if (novoPerfil != alvo.getPerfil()) {
                if (alvo.isTitular()) {
                    throw new NegocioException("A conta titular da empresa precisa continuar como Gestor.");
                }
                if (eleMesmo) {
                    throw new NegocioException("Você não pode alterar a sua própria categoria.");
                }
                alvo.setPerfil(novoPerfil);
            }
        }

        if (req.ativo() != null && req.ativo().booleanValue() != alvo.isAtivo()) {
            if (alvo.isTitular()) {
                throw new NegocioException("A conta titular da empresa não pode ser desativada.");
            }
            if (eleMesmo) {
                throw new NegocioException("Você não pode desativar a sua própria conta.");
            }
            alvo.setAtivo(req.ativo().booleanValue());
            if (!alvo.isAtivo()) {
                sessoes.deleteByUsuarioId(alvo.getId());
            }
        }

        if (req.senha() != null && !req.senha().isBlank()) {
            definirSenha(alvo, req.senha());
        }

        return UsuarioResponse.de(usuarios.save(alvo));
    }

    @Transactional
    public void redefinirSenhaTitular(UUID empresaId, String novaSenha) {
        Usuario titular = usuarios.findFirstByEmpresaIdAndTitularTrue(empresaId)
                .orElseThrow(() -> new NegocioException("Esta empresa não possui usuário titular."));
        definirSenha(titular, novaSenha);
        usuarios.save(titular);
    }

    private void definirSenha(Usuario usuario, String novaSenha) {
        validarSenha(novaSenha);
        usuario.setSenhaHash(SenhaUtil.hash(novaSenha));
        usuario.setTentativasFalhas(0);
        usuario.setBloqueadoAte(null);
        usuario.setResetTokenHash(null);
        usuario.setResetExpiraEm(null);
        sessoes.deleteByUsuarioId(usuario.getId());
    }

    private Usuario novoUsuario(UUID empresaId, String nome, String email, String senha, Perfil perfil, boolean titular) {
        String nomeLimpo = Texto.limpo(nome);
        if (nomeLimpo == null) {
            throw new NegocioException("Informe o nome do usuário.");
        }
        String emailLimpo = Texto.limpo(email);
        if (emailLimpo == null || !Texto.emailValido(emailLimpo)) {
            throw new NegocioException("Informe um e-mail válido para o usuário.");
        }
        emailLimpo = emailLimpo.toLowerCase();
        if (usuarios.existsByEmailIgnoreCase(emailLimpo)) {
            throw new NegocioException("Já existe um usuário com o e-mail " + emailLimpo + ".");
        }
        validarSenha(senha);

        Usuario u = new Usuario();
        u.setEmpresaId(empresaId);
        u.setNome(nomeLimpo);
        u.setEmail(emailLimpo);
        u.setSenhaHash(SenhaUtil.hash(senha));
        u.setPerfil(perfil);
        u.setTitular(titular);
        u.setAtivo(true);
        return usuarios.save(u);
    }

    private Perfil perfilDeEmpresa(String texto) {
        if (texto == null) {
            throw new NegocioException("Escolha a categoria do usuário.");
        }
        Perfil perfil;
        try {
            perfil = Perfil.valueOf(texto.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new NegocioException("Categoria inválida.");
        }
        if (perfil == Perfil.ADMIN) {
            throw new NegocioException("Categoria inválida.");
        }
        return perfil;
    }
}
