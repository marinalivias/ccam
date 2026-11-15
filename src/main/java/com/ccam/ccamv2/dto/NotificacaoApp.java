package com.ccam.ccamv2.dto;

import java.time.LocalDateTime;

public record NotificacaoApp(String id, String tipo, String titulo, String texto, LocalDateTime data) {
}
