package com.ccam.ccamv2.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "parabens",
        uniqueConstraints = @UniqueConstraint(columnNames = {"remetente_id", "destinatario_id", "ano"}),
        indexes = @Index(name = "idx_parabens_destinatario", columnList = "destinatario_id, criado_em"))
public class Parabens {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "empresa_id")
    private UUID empresaId;

    @Column(name = "remetente_id", nullable = false)
    private UUID remetenteId;

    @Column(name = "destinatario_id", nullable = false)
    private UUID destinatarioId;

    @Column(nullable = false)
    private int ano;

    private boolean lido;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm = LocalDateTime.now();
}
