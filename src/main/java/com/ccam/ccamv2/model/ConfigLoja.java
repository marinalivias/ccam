package com.ccam.ccamv2.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "config_loja")
public class ConfigLoja {
    @Id
    @Column(name = "empresa_id")
    private UUID empresaId;

    private boolean ativa;

    @Column(name = "desconto_aniversario")
    private int descontoAniversario = 10;
}
