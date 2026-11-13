package com.ccam.ccamv2.dto;

import com.ccam.ccamv2.model.Cliente;
import com.ccam.ccamv2.model.Financeiro;

import java.time.LocalDate;
import java.util.UUID;

public record FinanceiroResponse(
        UUID id,
        UUID clienteId,
        String alunoNome,
        boolean alunoAtivo,
        LocalDate vencimento,
        Double valor,
        String status) {
    public static FinanceiroResponse de(Financeiro f, Cliente aluno) {
        return new FinanceiroResponse(f.getId(), f.getClienteId(),
                aluno != null ? aluno.getNome() : "Aluno não encontrado",
                aluno != null && aluno.isAtivo(),
                f.getVencimento(), f.getValor(), f.getStatus());
    }
}
