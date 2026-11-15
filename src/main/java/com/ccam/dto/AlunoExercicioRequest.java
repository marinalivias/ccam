package com.ccam.dto;

public record AlunoExercicioRequest(
        Long catalogoId,
        String nome,
        String grupoMuscular,
        String diaSemana,
        String series,
        String repeticoes,
        String carga) {
}
