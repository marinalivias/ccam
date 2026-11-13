package com.ccam.ccamv2.dto;

import com.ccam.ccamv2.model.TreinoRealizado;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public record TreinoRealizadoResponse(
        LocalDate data,
        String diaSemana,
        int exerciciosPrevistos,
        int exerciciosFeitos,
        boolean concluido,
        List<String> idsFeitos,
        String observacao) {

    public static TreinoRealizadoResponse de(TreinoRealizado t) {
        List<String> ids = t.getIdsFeitos() == null || t.getIdsFeitos().isBlank()
                ? List.of()
                : Arrays.asList(t.getIdsFeitos().split(","));
        return new TreinoRealizadoResponse(t.getData(), t.getDiaSemana(), t.getExerciciosPrevistos(),
                t.getExerciciosFeitos(), t.isConcluido(), ids, t.getObservacao());
    }
}
