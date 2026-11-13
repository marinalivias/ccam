package com.ccam.ccamv2.dto;

public record ContatoRequest(
        String nome,
        String email,
        String telefone,
        String academia,
        String cidade,
        String mensagem,
        String site) {
}
