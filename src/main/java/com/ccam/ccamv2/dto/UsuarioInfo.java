package com.ccam.ccamv2.dto;

import java.util.List;
import java.util.UUID;

public record UsuarioInfo(
        UUID id,
        String nome,
        String email,
        String perfil,
        String perfilRotulo,
        boolean titular,
        UUID empresaId,
        String empresaNome,
        List<String> permissoes) {
}
