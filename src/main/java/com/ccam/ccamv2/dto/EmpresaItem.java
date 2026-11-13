package com.ccam.ccamv2.dto;

import com.ccam.ccamv2.model.Empresa;

public record EmpresaItem(Empresa empresa, String titularNome, String titularEmail) {
}
