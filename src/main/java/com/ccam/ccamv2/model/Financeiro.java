package com.ccam.ccamv2.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Entity
@Table(name = "financeiro")
public class Financeiro {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "empresa_id")
    private UUID empresaId;

    @Column(name = "cliente_id", nullable = false)
    private UUID clienteId;

    @Column(nullable = false)
    private LocalDate vencimento;

    @Column(nullable = false)
    private Double valor;

    @Column(nullable = false)
    private String status;
}
