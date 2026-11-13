package com.ccam.ccamv2.dto;

import com.ccam.ccamv2.model.Cliente;
import java.time.LocalDateTime;
import java.util.UUID;

public record ClienteResponse(
        UUID id,
        String nome,
        String email,
        String cpf,
        String telefone,
        Double peso,
        boolean ativo,
        LocalDateTime dataCadastro,
        LocalDateTime dataAtualizacao) {
    public static ClienteResponse de(Cliente c) {
        return new ClienteResponse(c.getId(), c.getNome(), c.getEmail(), c.getCpf(),
                c.getTelefone(), c.getPeso(), c.isAtivo(), c.getDataCadastro(), c.getDataAtualizacao());
    }
}
