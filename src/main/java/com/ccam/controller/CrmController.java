package com.ccam.controller;

import com.ccam.dto.CrmResumo;
import com.ccam.model.Cliente;
import com.ccam.model.Usuario;
import com.ccam.repository.ClienteRepository;
import com.ccam.repository.TreinoRealizadoRepository;
import com.ccam.security.AuthInterceptor;
import com.ccam.security.Permissao;
import com.ccam.security.RequerPermissao;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
@RequestMapping("/api/crm")
@CrossOrigin(origins = "*")
public class CrmController {
    private static final int DIAS_AUSENTE = 15;
    private static final int MAX_AUSENTES = 50;

    private final ClienteRepository clientes;
    private final TreinoRealizadoRepository realizados;
    private final com.ccam.repository.FinanceiroRepository financeiro;

    public CrmController(ClienteRepository clientes, TreinoRealizadoRepository realizados,
                         com.ccam.repository.FinanceiroRepository financeiro) {
        this.clientes = clientes;
        this.realizados = realizados;
        this.financeiro = financeiro;
    }

    @GetMapping("/financeiro")
    @RequerPermissao(Permissao.FINANCEIRO_VER)
    public Map<String, Object> financeiro(@RequestAttribute(AuthInterceptor.ATRIBUTO) Usuario usuario) {
        UUID empresaId = usuario.getEmpresaId();
        LocalDate hoje = LocalDate.now();
        YearMonth mes = YearMonth.now();
        List<com.ccam.model.Financeiro> todos = financeiro.findByEmpresaIdOrderByVencimentoDesc(empresaId);

        double recebidoMes = 0;
        double aReceber = 0;
        double vencido = 0;
        long informados = 0;
        Set<UUID> inadimplentes = new HashSet<>();
        Map<YearMonth, Double> recebidoPorMes = new TreeMap<>();
        Map<YearMonth, Double> lancadoPorMes = new TreeMap<>();
        for (int i = 5; i >= 0; i--) {
            recebidoPorMes.put(mes.minusMonths(i), 0.0);
            lancadoPorMes.put(mes.minusMonths(i), 0.0);
        }
        for (var f : todos) {
            double valor = f.getValor() == null ? 0 : f.getValor();
            boolean pago = "Pago".equals(f.getStatus());
            if (f.getVencimento() != null && lancadoPorMes.containsKey(YearMonth.from(f.getVencimento()))) {
                lancadoPorMes.merge(YearMonth.from(f.getVencimento()), valor, Double::sum);
            }
            if (pago) {
                LocalDate dia = f.getDataPagamento() != null ? f.getDataPagamento() : f.getVencimento();
                if (dia != null) {
                    YearMonth m = YearMonth.from(dia);
                    if (recebidoPorMes.containsKey(m)) {
                        recebidoPorMes.merge(m, valor, Double::sum);
                    }
                    if (m.equals(mes)) {
                        recebidoMes += valor;
                    }
                }
                continue;
            }
            if (f.getPagamentoInformadoEm() != null) {
                informados++;
            }
            if (f.getVencimento() == null) {
                continue;
            }
            if (f.getVencimento().isBefore(hoje)) {
                vencido += valor;
                inadimplentes.add(f.getClienteId());
            } else if (YearMonth.from(f.getVencimento()).equals(mes)) {
                aReceber += valor;
            }
        }
        long ativos = clientes.countByEmpresaIdAndAtivoTrue(empresaId);
        List<Map<String, Object>> meses = new ArrayList<>();
        for (YearMonth m : recebidoPorMes.keySet()) {
            meses.add(Map.of("mes", m.toString(), "recebido", recebidoPorMes.get(m), "lancado", lancadoPorMes.get(m)));
        }
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("recebidoMes", recebidoMes);
        r.put("aReceber", aReceber);
        r.put("vencido", vencido);
        r.put("inadimplentes", inadimplentes.size());
        r.put("percentualInadimplencia", ativos == 0 ? 0 : Math.round(inadimplentes.size() * 100.0 / ativos));
        r.put("informados", informados);
        r.put("meses", meses);
        return r;
    }

    @GetMapping("/resumo")
    @RequerPermissao(Permissao.CRM_VER)
    public CrmResumo resumo(@RequestAttribute(AuthInterceptor.ATRIBUTO) Usuario usuario) {
        UUID empresaId = usuario.getEmpresaId();
        LocalDate hoje = LocalDate.now();
        YearMonth mes = YearMonth.now();
        List<Cliente> ativos = clientes.findByEmpresaIdAndAtivoTrueOrderByNomeAsc(empresaId);

        long novatos = ativos.stream()
                .filter(c -> c.getDataCadastro() != null && YearMonth.from(c.getDataCadastro()).equals(mes))
                .count();
        long semana = realizados.contarAlunosComTreino(empresaId, hoje.minusDays(6), hoje);

        Map<UUID, LocalDate> ultimo = new HashMap<>();
        for (Object[] linha : realizados.ultimoTreinoPorAluno(empresaId)) {
            ultimo.put((UUID) linha[0], (LocalDate) linha[1]);
        }
        LocalDate limite = hoje.minusDays(DIAS_AUSENTE);
        List<CrmResumo.Ausente> ausentes = ativos.stream()
                .filter(c -> {
                    LocalDate u = ultimo.get(c.getId());
                    if (u != null) {
                        return u.isBefore(limite);
                    }
                    return c.getDataCadastro() == null || c.getDataCadastro().toLocalDate().isBefore(limite);
                })
                .map(c -> {
                    LocalDate u = ultimo.get(c.getId());
                    return new CrmResumo.Ausente(c.getId(), c.getNome(), u, u == null ? null : ChronoUnit.DAYS.between(u, hoje));
                })
                .sorted(Comparator.comparing(CrmResumo.Ausente::ultimoTreino, Comparator.nullsFirst(Comparator.naturalOrder())))
                .limit(MAX_AUSENTES)
                .toList();

        List<CrmResumo.Aniversariante> aniversariantes = ativos.stream()
                .filter(c -> c.getDataNascimento() != null && c.getDataNascimento().getMonthValue() == mes.getMonthValue())
                .sorted(Comparator.comparingInt(c -> c.getDataNascimento().getDayOfMonth()))
                .map(c -> new CrmResumo.Aniversariante(c.getId(), c.getNome(), c.getDataNascimento().getDayOfMonth(),
                        c.getDataNascimento().getDayOfMonth() == hoje.getDayOfMonth()))
                .toList();

        return new CrmResumo(ativos.size(), novatos, semana, ausentes, aniversariantes);
    }
}
