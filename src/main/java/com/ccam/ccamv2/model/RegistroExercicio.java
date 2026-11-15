package com.ccam.ccamv2.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "registros_exercicio",
        uniqueConstraints = @UniqueConstraint(columnNames = {"cliente_id", "data", "exercicio_id"}),
        indexes = @Index(name = "idx_registro_cliente_data", columnList = "cliente_id, data"))
public class RegistroExercicio {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "cliente_id", nullable = false)
    private UUID clienteId;

    @Column(nullable = false)
    private LocalDate data;

    @Column(name = "exercicio_id", nullable = false)
    private UUID exercicioId;

    @Column(name = "nome_exercicio")
    private String nomeExercicio;

    @Column(name = "series_previstas")
    private int seriesPrevistas;

    @Column(length = 200)
    private String reps;

    @Column(length = 40)
    private String carga;

    private boolean completo;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm = LocalDateTime.now();
}
