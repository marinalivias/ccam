package com.ccam.dto;

import com.ccam.model.Empresa;

public record EmpresaItem(Empresa empresa, String titularNome, String titularEmail) {
}
