package com.ccam.ccamv2.service;

import com.ccam.ccamv2.model.ConfigLoja;
import com.ccam.ccamv2.repository.ConfigLojaRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class LojaService {
    private final ConfigLojaRepository configs;

    public LojaService(ConfigLojaRepository configs) {
        this.configs = configs;
    }

    public ConfigLoja config(UUID empresaId) {
        return configs.findById(empresaId).orElseGet(() -> {
            ConfigLoja c = new ConfigLoja();
            c.setEmpresaId(empresaId);
            return c;
        });
    }

    public ConfigLoja salvar(ConfigLoja c) {
        return configs.save(c);
    }

    public static double comDesconto(double preco, int percentual) {
        return Math.round(preco * (100 - percentual)) / 100.0;
    }
}
