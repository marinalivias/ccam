package com.ccam.service;

import com.ccam.exception.NegocioException;
import com.ccam.model.Cliente;
import com.ccam.model.ConfigMensalidade;
import com.ccam.model.Financeiro;
import com.ccam.repository.ClienteRepository;
import com.ccam.repository.ConfigMensalidadeRepository;
import com.ccam.repository.FinanceiroRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class MensalidadeAutomaticaService {
    private static final Logger log = LoggerFactory.getLogger(MensalidadeAutomaticaService.class);
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ConfigMensalidadeRepository configs;
    private final ClienteRepository clientes;
    private final FinanceiroRepository financeiro;
    private final LogService logService;

    public MensalidadeAutomaticaService(ConfigMensalidadeRepository configs, ClienteRepository clientes,
                                        FinanceiroRepository financeiro, LogService logService) {
        this.configs = configs;
        this.clientes = clientes;
        this.financeiro = financeiro;
        this.logService = logService;
    }

    public ConfigMensalidade config(UUID empresaId) {
        return configs.findById(empresaId).orElseGet(() -> {
            ConfigMensalidade c = new ConfigMensalidade();
            c.setEmpresaId(empresaId);
            c.setAtiva(false);
            c.setDiaVencimento(10);
            return c;
        });
    }

    @Transactional
    public ConfigMensalidade salvar(UUID empresaId, boolean ativa, Double valor, Integer dia) {
        if (ativa && (valor == null || valor <= 0)) {
            throw new NegocioException("Informe o valor padrão da mensalidade.");
        }
        if (dia == null || dia < 1 || dia > 28) {
            throw new NegocioException("O dia de vencimento deve ser entre 1 e 28.");
        }
        ConfigMensalidade c = config(empresaId);
        c.setAtiva(ativa);
        c.setValor(valor);
        c.setDiaVencimento(dia);
        return configs.save(c);
    }

    public static LocalDate proximoVencimento(int dia, LocalDate hoje) {
        LocalDate desteMes = hoje.withDayOfMonth(dia);
        return desteMes.isBefore(hoje) ? desteMes.plusMonths(1) : desteMes;
    }

    @Transactional
    public int gerar(UUID empresaId) {
        ConfigMensalidade c = config(empresaId);
        if (!c.isAtiva() || c.getValor() == null || c.getDiaVencimento() == null) {
            return 0;
        }
        LocalDate vencimento = proximoVencimento(c.getDiaVencimento(), LocalDate.now());
        YearMonth mes = YearMonth.from(vencimento);
        int geradas = 0;
        for (Cliente aluno : clientes.findByEmpresaIdAndAtivoTrueOrderByNomeAsc(empresaId)) {
            List<Financeiro> doAluno = financeiro.findByClienteIdOrderByVencimentoDesc(aluno.getId());
            boolean jaTem = doAluno.stream().anyMatch(f -> f.getVencimento() != null && YearMonth.from(f.getVencimento()).equals(mes));
            if (jaTem) {
                continue;
            }
            double valor = doAluno.stream()
                    .filter(f -> f.getValor() != null && f.getVencimento() != null)
                    .max(Comparator.comparing(Financeiro::getVencimento))
                    .map(Financeiro::getValor)
                    .orElse(c.getValor());
            Financeiro f = new Financeiro();
            f.setEmpresaId(empresaId);
            f.setClienteId(aluno.getId());
            f.setVencimento(vencimento);
            f.setValor(valor);
            f.setStatus("Pendente");
            f = financeiro.save(f);
            logService.registrarPeloSistema(empresaId, LogService.FINANCEIRO, f.getId(), aluno.getId(), LogService.CRIACAO,
                    String.format(Locale.forLanguageTag("pt-BR"), "Mensalidade de R$ %,.2f com vencimento em %s gerada automaticamente.",
                            valor, vencimento.format(DATA)));
            geradas++;
        }
        if (geradas > 0) {
            log.info("{} mensalidade(s) gerada(s) para o vencimento {}", geradas, vencimento);
        }
        return geradas;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "0 0 6 * * *")
    public void gerarTodas() {
        for (ConfigMensalidade c : configs.findByAtivaTrue()) {
            try {
                gerar(c.getEmpresaId());
            } catch (RuntimeException e) {
                log.warn("Mensalidades automáticas não geradas para {}: {}", c.getEmpresaId(), e.getMessage());
            }
        }
    }
}
