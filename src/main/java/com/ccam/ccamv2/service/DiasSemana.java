package com.ccam.ccamv2.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public final class DiasSemana {
    public static final String ROTATIVO = "Rotativo (Ficha)";
    public static final List<String> NOMES = List.of(
            "Segunda-feira", "Terça-feira", "Quarta-feira", "Quinta-feira", "Sexta-feira", "Sábado", "Domingo");

    private DiasSemana() {
    }

    public static String de(LocalDate data) {
        return de(data.getDayOfWeek());
    }

    public static String de(DayOfWeek dia) {
        return NOMES.get(dia.getValue() - 1);
    }

    public static boolean valido(String dia) {
        return NOMES.contains(dia) || ROTATIVO.equals(dia);
    }
}
