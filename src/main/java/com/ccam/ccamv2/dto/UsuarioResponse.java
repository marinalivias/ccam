package com.ccam.ccamv2.dto;

import com.ccam.ccamv2.model.Usuario;
import java.time.LocalDateTime;
import java.util.UUID;

public record UsuarioResponse(
        UUID id,
        String nome,
        String email,
        String perfil,
        String perfilRotulo,
        boolean titular,
        boolean ativo,
        LocalDateTime ultimoAcesso,
        LocalDateTime dataCadastro) {
    public static UsuarioResponse de(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(),
                u.getPerfil().name(), u.getPerfil().getRotulo(),
                u.isTitular(), u.isAtivo(), u.getUltimoAcesso(), u.getDataCadastro());
    }
}
