package com.ccam.ccamv2.dto;

import java.util.List;
import java.util.UUID;

public record TreinoRealizadoRequest(List<UUID> idsFeitos, String observacao) {
}
