package com.ccam.ccamv2.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificacaoResponse(UUID id, String remetente, LocalDateTime criadoEm, boolean lido) {
}
