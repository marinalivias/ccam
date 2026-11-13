package com.ccam.ccamv2.dto;

import com.ccam.ccamv2.model.LogAlteracao;

import java.time.LocalDateTime;
import java.util.UUID;

public record LogResponse(
        UUID id,
        String entidade,
        String acao,
        String descricao,
        String usuarioNome,
        LocalDateTime dataHora) {
    public static LogResponse de(LogAlteracao l) {
        return new LogResponse(l.getId(), l.getEntidade(), l.getAcao(), l.getDescricao(),
                l.getUsuarioNome(), l.getDataHora());
    }
}
