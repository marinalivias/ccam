package com.ccam.ccamv2.config;

import com.ccam.ccamv2.model.Cliente;
import com.ccam.ccamv2.model.Empresa;
import com.ccam.ccamv2.model.Financeiro;
import com.ccam.ccamv2.model.Mural;
import com.ccam.ccamv2.model.Usuario;
import com.ccam.ccamv2.repository.ClienteRepository;
import com.ccam.ccamv2.repository.EmpresaRepository;
import com.ccam.ccamv2.repository.FinanceiroRepository;
import com.ccam.ccamv2.repository.MuralRepository;
import com.ccam.ccamv2.repository.UsuarioRepository;
import com.ccam.ccamv2.security.Perfil;
import com.ccam.ccamv2.security.SenhaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class DadosIniciais implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DadosIniciais.class);

    private final UsuarioRepository usuarios;
    private final EmpresaRepository empresas;
    private final ClienteRepository clientes;
    private final MuralRepository mural;
    private final FinanceiroRepository financeiro;

    private final String adminEmail;
    private final String adminSenha;
    private final String gestorEmail;
    private final String gestorSenha;

    public DadosIniciais(UsuarioRepository usuarios,
                         EmpresaRepository empresas,
                         ClienteRepository clientes,
                         MuralRepository mural,
                         FinanceiroRepository financeiro,
                         @Value("${ccam.admin.email}") String adminEmail,
                         @Value("${ccam.admin.senha}") String adminSenha,
                         @Value("${ccam.seed.gestor-email}") String gestorEmail,
                         @Value("${ccam.seed.gestor-senha}") String gestorSenha) {
        this.usuarios = usuarios;
        this.empresas = empresas;
        this.clientes = clientes;
        this.mural = mural;
        this.financeiro = financeiro;
        this.adminEmail = adminEmail;
        this.adminSenha = adminSenha;
        this.gestorEmail = gestorEmail;
        this.gestorSenha = gestorSenha;
    }

    @Override
    public void run(String... args) {
        criarAdmin();
        criarEmpresaPadrao();
        migrarDadosAntigos();
    }

    private void criarAdmin() {
        if (usuarios.existsByPerfil(Perfil.ADMIN)) {
            return;
        }
        Usuario admin = new Usuario();
        admin.setNome("Administrador do Sistema");
        admin.setEmail(adminEmail.trim().toLowerCase());
        admin.setSenhaHash(SenhaUtil.hash(adminSenha));
        admin.setPerfil(Perfil.ADMIN);
        admin.setTitular(false);
        admin.setAtivo(true);
        usuarios.save(admin);
        log.info("Usuário administrador criado: {} (troque a senha em 'Minha conta')", admin.getEmail());
    }

    private void criarEmpresaPadrao() {
        if (empresas.count() > 0) {
            return;
        }
        Empresa e = new Empresa();
        e.setRazaoSocial("Formula Fitness Academia");
        e.setNomeFantasia("Formula Fitness Academia");
        e.setCnpj("02602237000170");
        e.setProprietario("Jarbas Lopes de Queiroz");
        e.setAreasAtuacao("Musculação e Ergometria");
        e.setCidade("Goiânia");
        e.setUf("GO");
        e.setAtivo(true);
        e = empresas.save(e);

        Usuario gestor = new Usuario();
        gestor.setEmpresaId(e.getId());
        gestor.setNome("Gestor " + e.getNomeFantasia());
        gestor.setEmail(gestorEmail.trim().toLowerCase());
        gestor.setSenhaHash(SenhaUtil.hash(gestorSenha));
        gestor.setPerfil(Perfil.GESTOR);
        gestor.setTitular(true);
        gestor.setAtivo(true);
        usuarios.save(gestor);
        log.info("Empresa padrão criada: {} (gestor titular: {})", e.getNomeFantasia(), gestor.getEmail());
    }

    private void migrarDadosAntigos() {
        List<Empresa> todas = empresas.findAllByOrderByRazaoSocialAsc();
        if (todas.isEmpty()) {
            return;
        }
        UUID empresaId = todas.get(0).getId();

        int ajustados = 0;
        for (Cliente c : clientes.findAll()) {
            if (c.getEmpresaId() == null) {
                c.setEmpresaId(empresaId);
                clientes.save(c);
                ajustados++;
            }
        }
        for (Mural m : mural.findAll()) {
            if (m.getEmpresaId() == null) {
                m.setEmpresaId(empresaId);
                mural.save(m);
                ajustados++;
            }
        }
        for (Financeiro f : financeiro.findAll()) {
            if (f.getEmpresaId() == null) {
                f.setEmpresaId(empresaId);
                financeiro.save(f);
                ajustados++;
            }
        }
        if (ajustados > 0) {
            log.info("{} registro(s) antigo(s) vinculado(s) à empresa {}", ajustados, todas.get(0).getNomeFantasia());
        }
    }
}
