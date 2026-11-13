package com.ccam.ccamv2.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "treinos_realizados",
        uniqueConstraints = @UniqueConstraint(columnNames = {"cliente_id", "data"}),
        indexes = @Index(name = "idx_treino_realizado_empresa", columnList = "empresa_id, data"))
public class TreinoRealizado {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "empresa_id")
    private UUID empresaId;

    @Column(name = "cliente_id", nullable = false)
    private UUID clienteId;

    @Column(nullable = false)
    private LocalDate data;

    @Column(name = "dia_semana", length = 30)
    private String diaSemana;

    @Column(name = "exercicios_previstos")
    private int exerciciosPrevistos;

    @Column(name = "exercicios_feitos")
    private int exerciciosFeitos;

    @Column(name = "ids_feitos", columnDefinition = "TEXT")
    private String idsFeitos;

    private boolean concluido;

    @Column(length = 500)
    private String observacao;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm = LocalDateTime.now();
}
