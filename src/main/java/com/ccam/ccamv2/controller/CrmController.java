package com.ccam.ccamv2.controller;

import com.ccam.ccamv2.dto.CrmResumo;
import com.ccam.ccamv2.model.Cliente;
import com.ccam.ccamv2.model.Usuario;
import com.ccam.ccamv2.repository.ClienteRepository;
import com.ccam.ccamv2.repository.TreinoRealizadoRepository;
import com.ccam.ccamv2.security.AuthInterceptor;
import com.ccam.ccamv2.security.Permissao;
import com.ccam.ccamv2.security.RequerPermissao;
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

    public CrmController(ClienteRepository clientes, TreinoRealizadoRepository realizados) {
        this.clientes = clientes;
        this.realizados = realizados;
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
