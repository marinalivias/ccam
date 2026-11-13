package com.ccam.ccamv2.dto;

import java.util.List;

public record LojaAppResponse(
        boolean ativa,
        int descontoAniversario,
        boolean aniversarioHoje,
        List<ProdutoApp> produtos) {
}
