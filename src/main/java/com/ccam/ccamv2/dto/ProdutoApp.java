package com.ccam.ccamv2.dto;

import java.util.UUID;

public record ProdutoApp(
        UUID id,
        String nome,
        String descricao,
        String categoria,
        double preco,
        double precoFinal,
        boolean descontoAniversario) {
}
