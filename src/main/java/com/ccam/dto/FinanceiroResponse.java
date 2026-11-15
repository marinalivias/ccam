package com.ccam.dto;

import com.ccam.model.Cliente;
import com.ccam.model.Financeiro;

import java.time.LocalDate;
import java.util.UUID;

public record FinanceiroResponse(
        UUID id,
        UUID clienteId,
        String alunoNome,
        boolean alunoAtivo,
        LocalDate vencimento,
        Double valor,
        String status,
        LocalDate dataPagamento,
        java.time.LocalDateTime pagamentoInformadoEm) {
    public static FinanceiroResponse de(Financeiro f, Cliente aluno) {
        return new FinanceiroResponse(f.getId(), f.getClienteId(),
                aluno != null ? aluno.getNome() : "Aluno não encontrado",
                aluno != null && aluno.isAtivo(),
                f.getVencimento(), f.getValor(), f.getStatus(), f.getDataPagamento(), f.getPagamentoInformadoEm());
    }
}
