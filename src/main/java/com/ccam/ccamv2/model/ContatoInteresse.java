package com.ccam.ccamv2.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "contatos_interesse")
public class ContatoInteresse {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 160)
    private String email;

    @Column(length = 20)
    private String telefone;

    @Column(length = 160)
    private String academia;

    @Column(length = 120)
    private String cidade;

    @Column(length = 2000)
    private String mensagem;

    @Column(name = "data_envio", nullable = false)
    private LocalDateTime dataEnvio = LocalDateTime.now();

    @Column(columnDefinition = "boolean default false")
    private boolean atendido;
}
