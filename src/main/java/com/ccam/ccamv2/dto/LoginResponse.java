package com.ccam.ccamv2.dto;

import java.time.LocalDateTime;

public record LoginResponse(String token, LocalDateTime expiraEm, UsuarioInfo usuario) {
}
