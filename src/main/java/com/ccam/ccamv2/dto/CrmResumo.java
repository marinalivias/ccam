package com.ccam.ccamv2.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CrmResumo(
        long alunosAtivos,
        long novatosMes,
        long treinaramSemana,
        List<Ausente> ausentes,
        List<Aniversariante> aniversariantes) {

    public record Ausente(UUID id, String nome, LocalDate ultimoTreino, Long diasSemTreinar) {
    }

    public record Aniversariante(UUID id, String nome, int dia, boolean hoje) {
    }
}
