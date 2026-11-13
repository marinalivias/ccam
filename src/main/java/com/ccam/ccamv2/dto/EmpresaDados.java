package com.ccam.ccamv2.dto;

import java.time.LocalDate;

public record EmpresaDados(
        String razaoSocial,
        String nomeFantasia,
        String cnpj,
        String inscricaoEstadual,
        LocalDate dataFundacao,
        String proprietario,
        String areasAtuacao,
        String email,
        String telefone,
        String whatsapp,
        String site,
        String instagram,
        String horarioFuncionamento,
        Integer capacidadeAlunos,
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String uf,
        String observacoes) {
}
