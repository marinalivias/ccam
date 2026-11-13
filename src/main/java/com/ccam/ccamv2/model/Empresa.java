package com.ccam.ccamv2.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "empresas")
public class Empresa {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "razao_social", nullable = false)
    private String razaoSocial;

    @Column(name = "nome_fantasia")
    private String nomeFantasia;

    @Column(nullable = false, unique = true, length = 14)
    private String cnpj;

    @Column(name = "inscricao_estadual", length = 30)
    private String inscricaoEstadual;

    @Column(name = "data_fundacao")
    private LocalDate dataFundacao;

    private String proprietario;

    @Column(name = "areas_atuacao")
    private String areasAtuacao;

    private String email;

    @Column(length = 20)
    private String telefone;

    @Column(length = 20)
    private String whatsapp;

    private String site;

    private String instagram;

    @Column(name = "horario_funcionamento", length = 500)
    private String horarioFuncionamento;

    @Column(name = "capacidade_alunos")
    private Integer capacidadeAlunos;

    @Column(length = 8)
    private String cep;

    private String logradouro;

    @Column(length = 20)
    private String numero;

    private String complemento;

    private String bairro;

    private String cidade;

    @Column(length = 2)
    private String uf;

    @Column(length = 1000)
    private String observacoes;

    private boolean ativo = true;

    @Column(name = "data_cadastro")
    private LocalDateTime dataCadastro = LocalDateTime.now();
}
