package com.ccam.ccamv2.dto;

import com.ccam.ccamv2.model.Cliente;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ClienteDetalhe(
        UUID id,
        String nome,
        String email,
        String cpf,
        String telefone,
        String telefoneFixo,
        LocalDate dataNascimento,
        Double peso,
        Double altura,
        String sexo,
        String condicaoSaude,
        boolean ativo,
        LocalDateTime dataCadastro,
        LocalDateTime dataAtualizacao) {
    public static ClienteDetalhe de(Cliente c) {
        return new ClienteDetalhe(c.getId(), c.getNome(), c.getEmail(), c.getCpf(), c.getTelefone(),
                c.getTelefoneFixo(), c.getDataNascimento(), c.getPeso(), c.getAltura(), c.getSexo(),
                c.getCondicaoSaude(), c.isAtivo(), c.getDataCadastro(), c.getDataAtualizacao());
    }
}
