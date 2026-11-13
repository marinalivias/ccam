package com.ccam.ccamv2.config;

import com.ccam.ccamv2.model.Cliente;
import com.ccam.ccamv2.model.ConfigLoja;
import com.ccam.ccamv2.model.Empresa;
import com.ccam.ccamv2.model.Exercicio;
import com.ccam.ccamv2.model.Financeiro;
import com.ccam.ccamv2.model.Mural;
import com.ccam.ccamv2.model.Produto;
import com.ccam.ccamv2.model.Usuario;
import com.ccam.ccamv2.repository.ClienteRepository;
import com.ccam.ccamv2.repository.ConfigLojaRepository;
import com.ccam.ccamv2.repository.EmpresaRepository;
import com.ccam.ccamv2.repository.ExercicioRepository;
import com.ccam.ccamv2.repository.FinanceiroRepository;
import com.ccam.ccamv2.repository.MuralRepository;
import com.ccam.ccamv2.repository.ProdutoRepository;
import com.ccam.ccamv2.repository.UsuarioRepository;
import com.ccam.ccamv2.security.Perfil;
import com.ccam.ccamv2.security.SenhaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
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
    private final ExercicioRepository exercicios;
    private final ProdutoRepository produtos;
    private final ConfigLojaRepository configLoja;

    private final String adminEmail;
    private final String adminSenha;
    private final String gestorEmail;
    private final String gestorSenha;

    public DadosIniciais(UsuarioRepository usuarios,
                         EmpresaRepository empresas,
                         ClienteRepository clientes,
                         MuralRepository mural,
                         FinanceiroRepository financeiro,
                         ExercicioRepository exercicios,
                         ProdutoRepository produtos,
                         ConfigLojaRepository configLoja,
                         @Value("${ccam.admin.email}") String adminEmail,
                         @Value("${ccam.admin.senha}") String adminSenha,
                         @Value("${ccam.seed.gestor-email}") String gestorEmail,
                         @Value("${ccam.seed.gestor-senha}") String gestorSenha) {
        this.usuarios = usuarios;
        this.empresas = empresas;
        this.clientes = clientes;
        this.mural = mural;
        this.financeiro = financeiro;
        this.exercicios = exercicios;
        this.produtos = produtos;
        this.configLoja = configLoja;
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
        try {
            criarAlunoExemplo();
            criarLojaExemplo();
        } catch (RuntimeException e) {
            log.warn("Dados de exemplo não foram criados: {}", e.getMessage());
        }
    }

    private void criarLojaExemplo() {
        UUID empresaId = usuarios.findByEmailIgnoreCase(gestorEmail.trim()).map(Usuario::getEmpresaId).orElse(null);
        if (empresaId == null || configLoja.existsById(empresaId)) {
            return;
        }
        ConfigLoja config = new ConfigLoja();
        config.setEmpresaId(empresaId);
        config.setAtiva(true);
        config.setDescontoAniversario(15);
        configLoja.save(config);

        Object[][] itens = {
                {"Whey protein 900g", "Suplementos", 149.90, true, "Sabor chocolate"},
                {"Creatina 300g", "Suplementos", 89.90, true, null},
                {"Garrafa térmica 1L", "Acessórios", 49.90, true, "Mantém a água gelada por 12 horas"},
                {"Camiseta da academia", "Roupas", 59.90, false, "Tamanhos P, M, G e GG"},
                {"Barra de proteína", "Lanches", 9.90, false, null}
        };
        for (Object[] i : itens) {
            Produto p = new Produto();
            p.setEmpresaId(empresaId);
            p.setNome((String) i[0]);
            p.setCategoria((String) i[1]);
            p.setPreco((Double) i[2]);
            p.setDescontoAniversario((Boolean) i[3]);
            p.setDescricao((String) i[4]);
            p.setAtivo(true);
            produtos.save(p);
        }
        log.info("Loja de exemplo criada com {} produtos", itens.length);
    }

    private void criarAlunoExemplo() {
        String email = "aluno@ccam.com";
        String cpf = "11144477735";
        UUID empresaId = usuarios.findByEmailIgnoreCase(gestorEmail.trim()).map(Usuario::getEmpresaId).orElse(null);
        if (empresaId == null || clientes.findByEmailIgnoreCase(email).isPresent() || clientes.findByCpf(cpf).isPresent()) {
            return;
        }
        Cliente aluno = new Cliente();
        aluno.setEmpresaId(empresaId);
        aluno.setNome("Aluno Exemplo");
        aluno.setEmail(email);
        aluno.setCpf(cpf);
        aluno.setTelefone("(62) 99999-0000");
        aluno.setDataNascimento(LocalDate.now().withYear(2000));
        aluno.setSexo("M");
        aluno.setPeso(75.0);
        aluno.setAltura(1.75);
        aluno = clientes.save(aluno);

        String[][] ficha = {
                {"Segunda-feira", "Peito", "Supino reto", "4", "10", "20kg"},
                {"Segunda-feira", "Tríceps", "Tríceps corda", "3", "12", "15kg"},
                {"Quarta-feira", "Costas", "Puxada frontal", "4", "10", "30kg"},
                {"Quarta-feira", "Bíceps", "Rosca direta", "3", "12", "10kg"},
                {"Sexta-feira", "Pernas", "Agachamento livre", "4", "12", "30kg"},
                {"Sexta-feira", "Abdomen", "Prancha", "3", "45 segundos", null}
        };
        for (String[] l : ficha) {
            Exercicio ex = new Exercicio();
            ex.setClienteId(aluno.getId());
            ex.setDiaSemana(l[0]);
            ex.setGrupoMuscular(l[1]);
            ex.setNomeExercicio(l[2]);
            ex.setSeries(l[3]);
            ex.setRepeticoes(l[4]);
            ex.setCarga(l[5]);
            exercicios.save(ex);
        }
        log.info("Aluno de exemplo criado: {} (senha no app: o CPF)", email);
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
