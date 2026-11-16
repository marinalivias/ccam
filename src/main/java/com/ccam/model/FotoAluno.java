package com.ccam.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "fotos_aluno")
public class FotoAluno {
    @Id
    @Column(name = "cliente_id")
    private UUID clienteId;

    @Column(nullable = false, length = 20)
    private String tipo;

    @Column(nullable = false, columnDefinition = "bytea")
    private byte[] dados;

    @Column(name = "atualizada_em", nullable = false)
    private LocalDateTime atualizadaEm = LocalDateTime.now();
}
