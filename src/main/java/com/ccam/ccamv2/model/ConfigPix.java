package com.ccam.ccamv2.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "config_pix")
public class ConfigPix {
    @Id
    @Column(name = "empresa_id")
    private UUID empresaId;

    @Column(length = 77)
    private String chave;

    @Column(name = "nome_recebedor", length = 60)
    private String nomeRecebedor;

    @Column(length = 40)
    private String cidade;

    @Column(name = "dia_disponivel")
    private Integer diaDisponivel;
}
