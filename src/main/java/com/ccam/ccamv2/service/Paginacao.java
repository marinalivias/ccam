package com.ccam.ccamv2.service;

import com.ccam.ccamv2.exception.NegocioException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;

public final class Paginacao {
    public static final int TAMANHO_PADRAO = 10;
    public static final int TAMANHO_MINIMO = 5;
    public static final int TAMANHO_MAXIMO = 100;

    public static final LocalDate DATA_MINIMA = LocalDate.of(1900, 1, 1);
    public static final LocalDate DATA_MAXIMA = LocalDate.of(9999, 12, 31);

    private Paginacao() {
    }

    public static Pageable criar(Integer pagina, Integer tamanho, Sort ordem) {
        int p = pagina == null || pagina < 0 ? 0 : pagina;
        int t = tamanho == null ? TAMANHO_PADRAO : Math.max(TAMANHO_MINIMO, Math.min(TAMANHO_MAXIMO, tamanho));
        return PageRequest.of(p, t, ordem);
    }

    public static String termo(String busca) {
        if (busca == null) {
            return "";
        }
        String t = busca.trim().toLowerCase();
        return t.length() > 100 ? t.substring(0, 100) : t;
    }

    public static String like(String termo) {
        if (termo == null || termo.isEmpty()) {
            return "";
        }
        String escapado = termo.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return "%" + escapado + "%";
    }

    public static YearMonth mes(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return YearMonth.parse(valor.trim());
        } catch (DateTimeParseException e) {
            throw new NegocioException("Mês inválido. Use o formato AAAA-MM.");
        }
    }
}
