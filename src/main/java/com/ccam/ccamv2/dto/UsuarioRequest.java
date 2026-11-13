package com.ccam.ccamv2.dto;

public record UsuarioRequest(String nome, String email, String perfil, Boolean ativo, String senha) {
}
