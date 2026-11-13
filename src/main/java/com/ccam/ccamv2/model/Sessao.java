package com.ccam.ccamv2.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "sessoes")
public class Sessao {
    @Id
    @Column(name = "token_hash", length = 64)
    private String tokenHash;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "criada_em")
    private LocalDateTime criadaEm = LocalDateTime.now();

    @Column(name = "expira_em", nullable = false)
    private LocalDateTime expiraEm;
}
