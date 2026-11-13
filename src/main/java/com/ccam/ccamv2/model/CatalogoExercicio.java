package com.ccam.ccamv2.model;

import jakarta.persistence.*;

@Entity
@Table(name = "catalogo_exercicios")
public class CatalogoExercicio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(name = "grupo_muscular", nullable = false)
    private String grupoMuscular;

    @Column(name = "imagem_url")
    private String imagemUrl;

    public CatalogoExercicio() {}

    public Long getId() { return id; }
    public void setId(Long id) { this. id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getGrupoMuscular() { return grupoMuscular; }
    public void setGrupoMuscular(String grupoMuscular) { this.grupoMuscular = grupoMuscular; }

    public String getImagemUrl() { return imagemUrl; }
    public void setImagemUrl(String imagemUrl) { this.imagemUrl = imagemUrl; }
}
