package com.ccam.ccamv2.service;

import com.ccam.ccamv2.model.Cliente;
import com.ccam.ccamv2.model.LogAlteracao;
import com.ccam.ccamv2.model.Usuario;
import com.ccam.ccamv2.repository.LogAlteracaoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class LogService {
    public static final String ALUNO = "ALUNO";
    public static final String TREINO = "TREINO";
    public static final String FINANCEIRO = "FINANCEIRO";
    public static final String AVISO = "AVISO";
    public static final String PRODUTO = "PRODUTO";

    public static final String CRIACAO = "CRIACAO";
    public static final String EDICAO = "EDICAO";
    public static final String INATIVACAO = "INATIVACAO";
    public static final String REATIVACAO = "REATIVACAO";
    public static final String EXCLUSAO = "EXCLUSAO";

    private final LogAlteracaoRepository repository;

    public LogService(LogAlteracaoRepository repository) {
        this.repository = repository;
    }

    public void registrarPeloAluno(Cliente aluno, String entidade, UUID registroId, String acao, String descricao) {
        LogAlteracao log = new LogAlteracao();
        log.setEmpresaId(aluno.getEmpresaId());
        log.setEntidade(entidade);
        log.setRegistroId(registroId);
        log.setAlunoId(aluno.getId());
        log.setAcao(acao);
        log.setDescricao(descricao);
        log.setUsuarioNome(aluno.getNome() + " (aluno, pelo app)");
        log.setDataHora(LocalDateTime.now());
        repository.save(log);
    }

    public void registrar(Usuario autor, String entidade, UUID registroId, UUID alunoId, String acao, String descricao) {
        LogAlteracao log = new LogAlteracao();
        log.setEmpresaId(autor.getEmpresaId());
        log.setEntidade(entidade);
        log.setRegistroId(registroId);
        log.setAlunoId(alunoId);
        log.setAcao(acao);
        log.setDescricao(descricao);
        log.setUsuarioId(autor.getId());
        log.setUsuarioNome(autor.getNome());
        log.setDataHora(LocalDateTime.now());
        repository.save(log);
    }

    public static final class Diferencas {
        private final List<String> linhas = new ArrayList<>();

        public Diferencas campo(String rotulo, Object antes, Object depois) {
            String a = texto(antes);
            String d = texto(depois);
            if (!Objects.equals(a, d)) {
                linhas.add(rotulo + ": " + a + " para " + d);
            }
            return this;
        }

        public Diferencas sensivel(String rotulo, Object antes, Object depois) {
            if (!Objects.equals(texto(antes), texto(depois))) {
                linhas.add(rotulo + " alterado(a)");
            }
            return this;
        }

        public boolean vazio() {
            return linhas.isEmpty();
        }

        @Override
        public String toString() {
            return String.join("\n", linhas);
        }

        private static String texto(Object valor) {
            if (valor == null) {
                return "(vazio)";
            }
            String t = String.valueOf(valor).trim();
            return t.isEmpty() ? "(vazio)" : t;
        }
    }
}
