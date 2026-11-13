package com.ccam.ccamv2.dto;

public record AlunoExercicioRequest(
        Long catalogoId,
        String diaSemana,
        String series,
        String repeticoes,
        String carga) {
}
