package com.ccam.ccamv2.security;

import static com.ccam.ccamv2.security.Permissao.*;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum Perfil {
    ADMIN("Administrador", EnumSet.of(
            EMPRESAS_GERENCIAR)),

    GESTOR("Gestor", EnumSet.of(
            ALUNOS_VER, ALUNOS_EDITAR,
            TREINOS_VER, TREINOS_EDITAR,
            FINANCEIRO_VER, FINANCEIRO_LANCAR, FINANCEIRO_EDITAR,
            MURAL_VER, MURAL_PUBLICAR,
            CRM_VER,
            EMPRESA_VER, EMPRESA_EDITAR,
            LOJA_GERENCIAR,
            USUARIOS_GERENCIAR)),

    PROFESSOR("Professor", EnumSet.of(
            ALUNOS_VER,
            TREINOS_VER, TREINOS_EDITAR,
            MURAL_VER,
            EMPRESA_VER)),

    ATENDENTE("Atendente", EnumSet.of(
            ALUNOS_VER, ALUNOS_EDITAR,
            TREINOS_VER,
            FINANCEIRO_VER, FINANCEIRO_LANCAR,
            MURAL_VER, MURAL_PUBLICAR,
            CRM_VER,
            EMPRESA_VER));

    private final String rotulo;
    private final Set<Permissao> permissoes;

    Perfil(String rotulo, Set<Permissao> permissoes) {
        this.rotulo = rotulo;
        this.permissoes = Collections.unmodifiableSet(permissoes);
    }

    public String getRotulo() {
        return rotulo;
    }

    public Set<Permissao> getPermissoes() {
        return permissoes;
    }

    public boolean possui(Permissao permissao) {
        return permissoes.contains(permissao);
    }
}
