package com.ccam.ccamv2.dto;

import java.time.LocalDate;

public record FrequenciaResponse(
        int diasPorSemana,
        int treinosSemana,
        int treinos30Dias,
        int previstos30Dias,
        int percentual,
        int semanasSeguidas,
        LocalDate ultimoTreino) {
}
