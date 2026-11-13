package com.ccam.ccamv2.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "produtos")
public class Produto {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(length = 500)
    private String descricao;

    @Column(length = 60)
    private String categoria;

    @Column(nullable = false)
    private Double preco;

    @Column(columnDefinition = "boolean default true")
    private boolean ativo = true;

    @Column(name = "desconto_aniversario", columnDefinition = "boolean default false")
    private boolean descontoAniversario;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm = LocalDateTime.now();

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;
}
