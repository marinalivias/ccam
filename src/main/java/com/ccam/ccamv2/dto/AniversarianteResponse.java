package com.ccam.ccamv2.dto;

import java.util.UUID;

public record AniversarianteResponse(
        UUID id,
        String nome,
        int dia,
        int mes,
        boolean hoje,
        boolean voce,
        boolean parabensEnviado) {
}
