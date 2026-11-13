package com.ccam.ccamv2.dto;

import com.ccam.ccamv2.model.Cliente;

import java.time.LocalDate;
import java.util.UUID;

public record AlunoPerfil(
        UUID id,
        String nome,
        String email,
        String academia,
        LocalDate dataNascimento,
        Double peso,
        Double altura) {

    public static AlunoPerfil de(Cliente c, String academia) {
        return new AlunoPerfil(c.getId(), c.getNome(), c.getEmail(), academia,
                c.getDataNascimento(), c.getPeso(), c.getAltura());
    }
}
