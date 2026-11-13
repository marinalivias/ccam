package com.ccam.ccamv2.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "clientes")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "empresa_id")
    private UUID empresaId;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true, length = 14)
    private String cpf;

    @Column(length = 20)
    private String telefone;

    @Column(name = "telefone_fixo", length = 20)
    private String telefoneFixo;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    private Double peso;

    private Double altura;

    @Column(length = 20)
    private String sexo;

    @Column(name = "condicao_saude", length = 1000)
    private String condicaoSaude;

    @Column(name = "data_cadastro")
    private LocalDateTime dataCadastro = LocalDateTime.now();

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    @Column(columnDefinition = "boolean default true")
    private boolean ativo = true;
}
