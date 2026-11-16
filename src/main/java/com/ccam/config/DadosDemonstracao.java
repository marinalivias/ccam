package com.ccam.config;

import com.ccam.model.*;
import com.ccam.repository.*;
import com.ccam.service.DiasSemana;
import com.ccam.service.TreinoRealizadoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.MonthDay;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Order(2)
public class DadosDemonstracao implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DadosDemonstracao.class);
    private static final String ALUNO = DadosIniciais.ALUNO_EMAIL;

    private final boolean ativo;
    private final String gestorEmail;
    private final UsuarioRepository usuarios;
    private final ClienteRepository clientes;
    private final ExercicioRepository exercicios;
    private final MuralRepository mural;
    private final FinanceiroRepository financeiro;
    private final TreinoRealizadoRepository realizados;
    private final TreinoRealizadoService treinoRealizado;
    private final ConfigPixRepository configPix;
    private final ConfigMensalidadeRepository configMensalidade;
    private final com.ccam.service.MensalidadeAutomaticaService automaticas;

    public DadosDemonstracao(@Value("${ccam.demo:true}") boolean ativo,
                             @Value("${ccam.seed.gestor-email}") String gestorEmail,
                             UsuarioRepository usuarios, ClienteRepository clientes, ExercicioRepository exercicios,
                             MuralRepository mural, FinanceiroRepository financeiro, TreinoRealizadoRepository realizados,
                             TreinoRealizadoService treinoRealizado, ConfigPixRepository configPix,
                             ConfigMensalidadeRepository configMensalidade,
                             com.ccam.service.MensalidadeAutomaticaService automaticas) {
        this.ativo = ativo;
        this.gestorEmail = gestorEmail;
        this.usuarios = usuarios;
        this.clientes = clientes;
        this.exercicios = exercicios;
        this.mural = mural;
        this.financeiro = financeiro;
        this.realizados = realizados;
        this.treinoRealizado = treinoRealizado;
        this.configPix = configPix;
        this.configMensalidade = configMensalidade;
        this.automaticas = automaticas;
    }

    @Override
    public void run(String... args) {
        if (!ativo) {
            return;
        }
        try {
            preparar();
        } catch (RuntimeException e) {
            log.warn("Dados de demonstração não foram preparados: {}", e.getMessage());
        }
    }

    private void preparar() {
        Optional<Usuario> gestor = usuarios.findByEmailIgnoreCase(gestorEmail.trim());
        if (gestor.isEmpty() || gestor.get().getEmpresaId() == null) {
            return;
        }
        UUID empresaId = gestor.get().getEmpresaId();
        Cliente aluno = clientes.findByEmailIgnoreCase(ALUNO).filter(c -> empresaId.equals(c.getEmpresaId())).orElse(null);
        if (aluno == null) {
            return;
        }
        LocalDate hoje = LocalDate.now();

        if (aluno.getDataCadastro() == null || aluno.getDataCadastro().toLocalDate().isAfter(hoje.minusDays(60))) {
            aluno.setDataCadastro(hoje.minusDays(60).atTime(9, 0));
            aluno = clientes.save(aluno);
        }
        aniversarioHoje(aluno, hoje);
        avisos(empresaId, gestor.get().getNome());
        mensalidades(empresaId, aluno, hoje);
        treinos(aluno, hoje);
        pixDeExemplo(empresaId);
        historicoFinanceiro(empresaId, aluno.getId(), hoje);
        log.info("Dados de demonstração prontos: {} faz aniversário hoje", ALUNO);
    }

    private void aniversarioHoje(Cliente c, LocalDate hoje) {
        int ano = c.getDataNascimento() != null ? c.getDataNascimento().getYear() : 2000;
        MonthDay dia = MonthDay.from(hoje);
        LocalDate nova = dia.isValidYear(ano) ? dia.atYear(ano) : LocalDate.of(ano, hoje.getMonthValue(), 28);
        if (!nova.equals(c.getDataNascimento())) {
            c.setDataNascimento(nova);
            clientes.save(c);
        }
    }

    private void avisos(UUID empresaId, String autor) {
        if (!mural.findByEmpresaIdOrderByDataPublicacaoDesc(empresaId).isEmpty()) {
            return;
        }
        String[][] lista = {
                {"Horário especial no feriado", "No próximo feriado a academia abre das 8h às 14h. Aproveite para treinar com a gente!", "2"},
                {"Novas aulas de funcional", "A partir da semana que vem teremos aulas de funcional às terças e quintas, às 19h. Vagas limitadas, procure a recepção.", "6"}
        };
        for (String[] a : lista) {
            Mural m = new Mural();
            m.setEmpresaId(empresaId);
            m.setTitulo(a[0]);
            m.setMensagem(a[1]);
            m.setAutorNome(autor);
            m.setDataPublicacao(LocalDateTime.now().minusDays(Long.parseLong(a[2])));
            mural.save(m);
        }
    }

    private void mensalidades(UUID empresaId, Cliente aluno, LocalDate hoje) {
        if (!financeiro.findByClienteIdOrderByVencimentoDesc(aluno.getId()).isEmpty()) {
            return;
        }
        Object[][] lista = {
                {hoje.minusMonths(1).withDayOfMonth(10), "Pago"},
                {hoje.minusDays(10), "Pendente"},
                {hoje.plusDays(15), "Pendente"}
        };
        for (Object[] l : lista) {
            Financeiro f = new Financeiro();
            f.setEmpresaId(empresaId);
            f.setClienteId(aluno.getId());
            f.setValor(129.90);
            f.setVencimento((LocalDate) l[0]);
            f.setStatus((String) l[1]);
            if ("Pago".equals(l[1])) {
                f.setDataPagamento(((LocalDate) l[0]).minusDays(2));
            }
            financeiro.save(f);
        }
    }

    private void treinos(Cliente aluno, LocalDate hoje) {
        if (!realizados.findByClienteIdAndDataBetweenOrderByDataAsc(aluno.getId(), hoje.minusDays(27), hoje).isEmpty()) {
            return;
        }
        List<Exercicio> ficha = exercicios.findByClienteId(aluno.getId());
        boolean parcialFeito = false;
        int contador = 0;
        for (LocalDate d = hoje.minusDays(1); !d.isBefore(hoje.minusDays(27)); d = d.minusDays(1)) {
            String dia = DiasSemana.de(d);
            List<Exercicio> doDia = ficha.stream().filter(e -> dia.equals(e.getDiaSemana())).toList();
            if (doDia.isEmpty()) {
                continue;
            }
            contador++;
            if (contador % 4 == 0) {
                continue;
            }
            if (!parcialFeito) {
                Exercicio primeiro = doDia.get(0);
                int series = TreinoRealizadoService.series(primeiro);
                Integer alvo = TreinoRealizadoService.alvoReps(primeiro);
                int meta = alvo == null ? 1 : alvo;
                List<Integer> reps = new java.util.ArrayList<>();
                for (int i = 0; i < series; i++) {
                    reps.add(i < series - 1 ? meta : Math.max(0, meta - 4));
                }
                treinoRealizado.registrar(aluno, d, primeiro.getId(), reps, primeiro.getCarga());
                parcialFeito = true;
                continue;
            }
            int semanasAtras = (int) java.time.temporal.ChronoUnit.DAYS.between(d, hoje) / 7;
            for (Exercicio ex : doDia) {
                Integer alvo = TreinoRealizadoService.alvoReps(ex);
                int meta = alvo == null ? 1 : alvo;
                List<Integer> reps = new java.util.ArrayList<>();
                for (int i = 0; i < TreinoRealizadoService.series(ex); i++) {
                    reps.add(meta);
                }
                treinoRealizado.registrar(aluno, d, ex.getId(), reps, cargaDaSemana(ex.getCarga(), semanasAtras));
            }
        }
    }

    private static String cargaDaSemana(String carga, int semanasAtras) {
        if (carga == null) {
            return null;
        }
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\d+").matcher(carga);
        if (!m.find()) {
            return carga;
        }
        int base = Integer.parseInt(m.group());
        int valor = Math.max(1, (int) Math.round(base * (1 - 0.08 * semanasAtras)));
        return valor + "kg";
    }

    private void historicoFinanceiro(UUID empresaId, UUID alunoExemplo, LocalDate hoje) {
        if (configMensalidade.existsById(empresaId)) {
            return;
        }
        List<Cliente> outros = clientes.findByEmpresaIdAndAtivoTrueOrderByNomeAsc(empresaId).stream()
                .filter(c -> !c.getId().equals(alunoExemplo))
                .toList();
        double[] valores = {99.90, 129.90, 149.90};
        for (int i = 0; i < outros.size(); i++) {
            Cliente c = outros.get(i);
            boolean novato = i >= outros.size() - 2;
            int meses = novato ? 0 : 3 + i % 3;
            if (!novato && c.getDataCadastro() != null && c.getDataCadastro().toLocalDate().isAfter(hoje.minusMonths(meses))) {
                c.setDataCadastro(hoje.minusMonths(meses).withDayOfMonth(5).atTime(9, 0));
                clientes.save(c);
            }
            if (!financeiro.findByClienteIdOrderByVencimentoDesc(c.getId()).isEmpty()) {
                continue;
            }
            double valor = valores[i % valores.length];
            for (int m = meses; m >= 0; m--) {
                LocalDate venc = hoje.minusMonths(m).withDayOfMonth(10);
                Financeiro f = new Financeiro();
                f.setEmpresaId(empresaId);
                f.setClienteId(c.getId());
                f.setVencimento(venc);
                f.setValor(valor);
                boolean atrasado = m == 0 && i % 4 == 1;
                if (venc.isBefore(hoje) && !atrasado) {
                    f.setStatus("Pago");
                    f.setDataPagamento(venc.minusDays((i + m) % 4));
                } else {
                    f.setStatus("Pendente");
                }
                financeiro.save(f);
            }
        }
        ConfigMensalidade config = new ConfigMensalidade();
        config.setEmpresaId(empresaId);
        config.setAtiva(true);
        config.setValor(129.90);
        config.setDiaVencimento(10);
        configMensalidade.save(config);
        automaticas.gerar(empresaId);
    }

    private void pixDeExemplo(UUID empresaId) {
        if (configPix.existsById(empresaId)) {
            configPix.findById(empresaId).filter(c -> c.getDiaDisponivel() == null).ifPresent(c -> {
                c.setDiaDisponivel(1);
                configPix.save(c);
            });
            return;
        }
        ConfigPix c = new ConfigPix();
        c.setDiaDisponivel(1);
        c.setEmpresaId(empresaId);
        c.setChave("pix@formulafitness.com.br");
        c.setNomeRecebedor("Formula Fitness Academia");
        c.setCidade("Goiânia");
        configPix.save(c);
    }
}
