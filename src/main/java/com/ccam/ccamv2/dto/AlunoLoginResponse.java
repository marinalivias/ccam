package com.ccam.ccamv2.dto;

import java.time.LocalDateTime;

public record AlunoLoginResponse(String token, LocalDateTime expiraEm, AlunoPerfil aluno) {
}
