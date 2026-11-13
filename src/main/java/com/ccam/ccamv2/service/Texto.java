package com.ccam.ccamv2.service;

final class Texto {
    private Texto() {
    }

    static String limpo(String valor) {
        if (valor == null) {
            return null;
        }
        String t = valor.trim();
        return t.isEmpty() ? null : t;
    }

    static String soNumeros(String valor) {
        return valor == null ? "" : valor.replaceAll("[^0-9]", "");
    }

    static boolean emailValido(String email) {
        return email != null && email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    }
}
