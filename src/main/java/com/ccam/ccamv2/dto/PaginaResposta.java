package com.ccam.ccamv2.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

public record PaginaResposta<T>(
        List<T> itens,
        int pagina,
        int tamanho,
        long totalItens,
        int totalPaginas) {
    public static <E, T> PaginaResposta<T> de(Page<E> page, Function<E, T> conversor) {
        return new PaginaResposta<>(
                page.getContent().stream().map(conversor).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }

    public static <T> PaginaResposta<T> de(Page<T> page) {
        return de(page, Function.identity());
    }
}
