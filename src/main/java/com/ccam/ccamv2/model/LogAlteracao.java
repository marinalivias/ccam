package com.ccam.ccamv2.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "logs_alteracao", indexes = {
        @Index(name = "idx_log_aluno", columnList = "aluno_id, data_hora"),
        @Index(name = "idx_log_empresa", columnList = "empresa_id, data_hora")
})
public class LogAlteracao {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "empresa_id")
    private UUID empresaId;

    @Column(nullable = false, length = 20)
    private String entidade;

    @Column(name = "registro_id")
    private UUID registroId;

    @Column(name = "aluno_id")
    private UUID alunoId;

    @Column(nullable = false, length = 20)
    private String acao;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Column(name = "usuario_nome")
    private String usuarioNome;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora = LocalDateTime.now();
}
