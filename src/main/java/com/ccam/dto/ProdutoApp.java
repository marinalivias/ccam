package com.ccam.dto;

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
