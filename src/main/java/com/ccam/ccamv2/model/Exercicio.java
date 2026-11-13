package com.ccam.ccamv2.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Data
@Entity
@Table(name = "exercicios")
public class Exercicio {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "cliente_id", nullable = false)
    private UUID clienteId;

    @Column(name = "dia_semana", nullable = false)
    private String diaSemana;

    @Column(name = "grupo_muscular")
    private String grupoMuscular;

    @Column(name = "nome_exercicio", nullable = false)
    private String nomeExercicio;

    private String series;
    private String repeticoes;
    private String carga;

    @Column(name = "imagem_url")
    private String imagemUrl;
}
