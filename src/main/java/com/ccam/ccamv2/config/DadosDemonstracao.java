package com.ccam.ccamv2.config;

import com.ccam.ccamv2.model.*;
import com.ccam.ccamv2.repository.*;
import com.ccam.ccamv2.service.DiasSemana;
import com.ccam.ccamv2.service.TreinoRealizadoService;
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
    private static final String ALUNO = "aluno@ccam.com";
    private static final String COLEGA = "colega@ccam.com";
    private static final String CPF_COLEGA = "39053344705";

    private final boolean ativo;
    private final String gestorEmail;
    private final UsuarioRepository usuarios;
    private final ClienteRepository clientes;
    private final ExercicioRepository exercicios;
    private final MuralRepository mural;
    private final FinanceiroRepository financeiro;
    private final TreinoRealizadoRepository realizados;
    private final ParabensRepository parabens;
    private final TreinoRealizadoService treinoRealizado;

    public DadosDemonstracao(@Value("${ccam.demo:true}") boolean ativo,
                             @Value("${ccam.seed.gestor-email}") String gestorEmail,
                             UsuarioRepository usuarios, ClienteRepository clientes, ExercicioRepository exercicios,
                             MuralRepository mural, FinanceiroRepository financeiro, TreinoRealizadoRepository realizados,
                             ParabensRepository parabens, TreinoRealizadoService treinoRealizado) {
        this.ativo = ativo;
        this.gestorEmail = gestorEmail;
        this.usuarios = usuarios;
        this.clientes = clientes;
        this.exercicios = exercicios;
        this.mural = mural;
        this.financeiro = financeiro;
        this.realizados = realizados;
        this.parabens = parabens;
        this.treinoRealizado = treinoRealizado;
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
        Cliente colega = colega(empresaId);
        LocalDate hoje = LocalDate.now();

        if (aluno.getDataCadastro() == null || aluno.getDataCadastro().toLocalDate().isAfter(hoje.minusDays(60))) {
            aluno.setDataCadastro(hoje.minusDays(60).atTime(9, 0));
            aluno = clientes.save(aluno);
        }
        aniversarioHoje(aluno, hoje);
        if (colega != null) {
            aniversarioHoje(colega, hoje);
        }
        avisos(empresaId, gestor.get().getNome());
        mensalidades(empresaId, aluno, hoje);
        treinos(aluno, hoje);
        if (colega != null) {
            parabensDoColega(empresaId, colega, aluno, hoje);
        }
        log.info("Dados de demonstração prontos: {} e {} fazem aniversário hoje", ALUNO, COLEGA);
    }

    private Cliente colega(UUID empresaId) {
        Optional<Cliente> existente = clientes.findByEmailIgnoreCase(COLEGA);
        if (existente.isPresent()) {
            return empresaId.equals(existente.get().getEmpresaId()) ? existente.get() : null;
        }
        if (clientes.findByCpf(CPF_COLEGA).isPresent()) {
            return null;
        }
        Cliente c = new Cliente();
        c.setEmpresaId(empresaId);
        c.setNome("Colega Exemplo");
        c.setEmail(COLEGA);
        c.setCpf(CPF_COLEGA);
        c.setTelefone("(62) 98888-0000");
        c.setSexo("F");
        c.setDataNascimento(LocalDate.of(1998, 1, 1));
        c.setDataCadastro(LocalDateTime.now().minusDays(40));
        return clientes.save(c);
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
            treinoRealizado.marcarCompletos(aluno, d, doDia.stream().map(Exercicio::getId).toList(), null);
        }
    }

    private void parabensDoColega(UUID empresaId, Cliente colega, Cliente aluno, LocalDate hoje) {
        parabens.apagarDe(colega.getId(), aluno.getId());
        Parabens p = new Parabens();
        p.setEmpresaId(empresaId);
        p.setRemetenteId(colega.getId());
        p.setDestinatarioId(aluno.getId());
        p.setAno(hoje.getYear());
        p.setCriadoEm(LocalDateTime.now().minusHours(1));
        parabens.save(p);
    }
}
