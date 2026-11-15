package com.ccam.service;

import com.ccam.exception.NegocioException;
import com.ccam.model.ConfigPix;
import com.ccam.repository.ConfigPixRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class PixService {
    private final ConfigPixRepository configs;

    public PixService(ConfigPixRepository configs) {
        this.configs = configs;
    }

    public Optional<ConfigPix> config(UUID empresaId) {
        return configs.findById(empresaId).filter(c -> c.getChave() != null && !c.getChave().isBlank());
    }

    public Optional<ConfigPix> registro(UUID empresaId) {
        return configs.findById(empresaId);
    }

    public ConfigPix salvar(UUID empresaId, String chave, String nome, String cidade, Integer dia) {
        ConfigPix c = configs.findById(empresaId).orElseGet(() -> {
            ConfigPix n = new ConfigPix();
            n.setEmpresaId(empresaId);
            return n;
        });
        if (dia != null && (dia < 1 || dia > 28)) {
            throw new NegocioException("O dia de liberação do Pix deve ser entre 1 e 28.");
        }
        c.setDiaDisponivel(dia);
        String k = limpo(chave);
        if (k == null) {
            c.setChave(null);
            return configs.save(c);
        }
        if (k.length() > 77) {
            throw new NegocioException("A chave Pix é longa demais.");
        }
        String n = limpo(nome);
        String cid = limpo(cidade);
        if (n == null || cid == null) {
            throw new NegocioException("Informe o nome de quem recebe e a cidade.");
        }
        c.setChave(k);
        c.setNomeRecebedor(n);
        c.setCidade(cid);
        return configs.save(c);
    }

    public String copiaECola(ConfigPix c, double valor, String identificador) {
        String conta = campo("00", "br.gov.bcb.pix") + campo("01", c.getChave());
        String txid = semAcento(identificador).replaceAll("[^A-Za-z0-9]", "");
        if (txid.isEmpty()) {
            txid = "***";
        }
        String payload = campo("00", "01")
                + campo("26", conta)
                + campo("52", "0000")
                + campo("53", "986")
                + campo("54", String.format(Locale.US, "%.2f", valor))
                + campo("58", "BR")
                + campo("59", corta(semAcento(c.getNomeRecebedor()).toUpperCase(Locale.ROOT), 25))
                + campo("60", corta(semAcento(c.getCidade()).toUpperCase(Locale.ROOT), 15))
                + campo("62", campo("05", corta(txid, 25)))
                + "6304";
        return payload + crc16(payload);
    }

    private static String campo(String id, String valor) {
        return id + String.format("%02d", valor.length()) + valor;
    }

    private static String crc16(String texto) {
        int crc = 0xFFFF;
        for (byte b : texto.getBytes(StandardCharsets.UTF_8)) {
            crc ^= (b & 0xFF) << 8;
            for (int i = 0; i < 8; i++) {
                crc = (crc & 0x8000) != 0 ? (crc << 1) ^ 0x1021 : crc << 1;
                crc &= 0xFFFF;
            }
        }
        return String.format("%04X", crc);
    }

    private static String semAcento(String t) {
        return Normalizer.normalize(t == null ? "" : t, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }

    private static String corta(String t, int max) {
        return t.length() > max ? t.substring(0, max) : t;
    }

    private static String limpo(String v) {
        if (v == null) {
            return null;
        }
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }
}
