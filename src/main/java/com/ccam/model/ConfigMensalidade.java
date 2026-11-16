package com.ccam.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "config_mensalidade")
public class ConfigMensalidade {
    @Id
    @Column(name = "empresa_id")
    private UUID empresaId;

    @Column(nullable = false)
    private boolean ativa;

    private Double valor;

    @Column(name = "dia_vencimento")
    private Integer diaVencimento;
}
