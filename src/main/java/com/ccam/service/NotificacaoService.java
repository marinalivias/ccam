package com.ccam.service;

import com.ccam.dto.NotificacaoApp;
import com.ccam.model.Cliente;
import com.ccam.model.ConfigLoja;
import com.ccam.model.ConfigPix;
import com.ccam.model.Financeiro;
import com.ccam.model.LogAlteracao;
import com.ccam.model.Mural;
import com.ccam.repository.FinanceiroRepository;
import com.ccam.repository.LogAlteracaoRepository;
import com.ccam.repository.MuralRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class NotificacaoService {
    private static final int DIAS = 30;
    private static final int DIAS_SEM_TREINO = 7;
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM");
    private static final Locale BR = Locale.forLanguageTag("pt-BR");

    private final FinanceiroRepository financeiro;
    private final MuralRepository mural;
    private final LogAlteracaoRepository logs;
    private final LojaService lojaService;
    private final PixService pixService;
    private final com.ccam.repository.ExercicioRepository exercicios;
    private final com.ccam.repository.TreinoRealizadoRepository realizados;

    public NotificacaoService(FinanceiroRepository financeiro, MuralRepository mural, LogAlteracaoRepository logs,
                              LojaService lojaService, PixService pixService,
                              com.ccam.repository.ExercicioRepository exercicios,
                              com.ccam.repository.TreinoRealizadoRepository realizados) {
        this.financeiro = financeiro;
        this.mural = mural;
        this.logs = logs;
        this.lojaService = lojaService;
        this.pixService = pixService;
        this.exercicios = exercicios;
        this.realizados = realizados;
    }

    public List<NotificacaoApp> doAluno(Cliente aluno) {
        LocalDate hoje = LocalDate.now();
        LocalDateTime limite = hoje.minusDays(DIAS).atStartOfDay();
        List<NotificacaoApp> lista = new ArrayList<>();
        mensalidades(aluno, hoje, lista);
        aniversario(aluno, hoje, lista);
        avisos(aluno, limite, lista);
        treino(aluno, limite, lista);
        inatividade(aluno, hoje, lista);
        lista.sort(Comparator.comparing(NotificacaoApp::data).reversed());
        return lista.size() > 40 ? lista.subList(0, 40) : lista;
    }

    private void mensalidades(Cliente aluno, LocalDate hoje, List<NotificacaoApp> lista) {
        ConfigPix pix = pixService.config(aluno.getEmpresaId()).orElse(null);
        Integer diaPix = pix == null ? null : pix.getDiaDisponivel();
        for (Financeiro f : financeiro.findByClienteIdOrderByVencimentoDesc(aluno.getId())) {
            String valor = String.format(BR, "R$ %,.2f", f.getValor());
            LocalDate venc = f.getVencimento();
            if ("Pago".equals(f.getStatus())) {
                LocalDate pago = f.getDataPagamento();
                if (pago != null && !pago.isBefore(hoje.minusDays(DIAS))) {
                    lista.add(new NotificacaoApp("pago:" + f.getId(), "pagamento", "Pagamento confirmado",
                            "A academia confirmou o pagamento da mensalidade de " + valor + " com vencimento em " + venc.format(DATA) + ".",
                            pago.atTime(9, 0)));
                }
                continue;
            }
            if (f.getPagamentoInformadoEm() != null || venc == null) {
                continue;
            }
            long dias = ChronoUnit.DAYS.between(hoje, venc);
            if (dias < 0) {
                long atraso = -dias;
                long ciclo = (atraso - 1) / 5;
                lista.add(new NotificacaoApp("vencida:" + f.getId() + ":" + ciclo, "pagamento", "Mensalidade vencida",
                        "Sua mensalidade de " + valor + " venceu em " + venc.format(DATA) + ". Regularize pelo app ou na recepção.",
                        venc.plusDays(1 + ciclo * 5).atTime(9, 0)));
                continue;
            }
            if (dias <= 3) {
                lista.add(new NotificacaoApp("vence:" + f.getId(), "pagamento",
                        dias == 0 ? "Mensalidade vence hoje" : "Mensalidade perto de vencer",
                        dias == 0 ? "Sua mensalidade de " + valor + " vence hoje. Pague pelo app com Pix."
                                : "Sua mensalidade de " + valor + " vence em " + venc.format(DATA) + ". Pague pelo app com Pix.",
                        venc.minusDays(Math.min(dias, 3)).atTime(9, 0)));
            }
            if (pix != null && diaPix != null) {
                LocalDate libera = venc.withDayOfMonth(Math.min(diaPix, venc.lengthOfMonth()));
                if (libera.isAfter(venc)) {
                    libera = libera.minusMonths(1);
                }
                if (!libera.isAfter(hoje)) {
                    lista.add(new NotificacaoApp("pix:" + f.getId(), "pagamento", "Pix disponível",
                            "Já dá para pagar pelo app a mensalidade de " + valor + " com vencimento em " + venc.format(DATA) + ".",
                            libera.atTime(9, 0)));
                }
            }
        }
    }

    private void aniversario(Cliente aluno, LocalDate hoje, List<NotificacaoApp> lista) {
        LocalDate nasc = aluno.getDataNascimento();
        if (nasc == null || nasc.getMonthValue() != hoje.getMonthValue() || nasc.getDayOfMonth() != hoje.getDayOfMonth()) {
            return;
        }
        ConfigLoja loja = lojaService.config(aluno.getEmpresaId());
        String texto = loja.isAtiva() && loja.getDescontoAniversario() > 0
                ? "Hoje você tem " + loja.getDescontoAniversario() + "% de desconto nos produtos marcados da loja."
                : "A equipe da academia deseja um ótimo dia para você.";
        lista.add(new NotificacaoApp("aniversario:" + hoje.getYear(), "aniversario", "Feliz aniversário!", texto, hoje.atStartOfDay()));
    }

    private void avisos(Cliente aluno, LocalDateTime limite, List<NotificacaoApp> lista) {
        for (Mural m : mural.findByEmpresaIdOrderByDataPublicacaoDesc(aluno.getEmpresaId())) {
            if (m.getDataPublicacao() == null || m.getDataPublicacao().isBefore(limite)) {
                break;
            }
            lista.add(new NotificacaoApp("aviso:" + m.getId(), "aviso", "Novo aviso: " + m.getTitulo(),
                    resumo(m.getMensagem()), m.getDataPublicacao()));
        }
    }

    private void treino(Cliente aluno, LocalDateTime limite, List<NotificacaoApp> lista) {
        Map<String, List<LogAlteracao>> porDia = new LinkedHashMap<>();
        for (LogAlteracao l : logs.findByAlunoIdAndEntidadeAndUsuarioIdIsNotNullAndDataHoraAfterOrderByDataHoraDesc(
                aluno.getId(), LogService.TREINO, limite)) {
            porDia.computeIfAbsent(l.getDataHora().toLocalDate() + ":" + l.getUsuarioId(), k -> new ArrayList<>()).add(l);
        }
        for (Map.Entry<String, List<LogAlteracao>> e : porDia.entrySet()) {
            LogAlteracao ultimo = e.getValue().get(0);
            int n = e.getValue().size();
            lista.add(new NotificacaoApp("treino:" + e.getKey(), "treino", "Sua ficha foi atualizada",
                    ultimo.getUsuarioNome() + (n == 1 ? " fez uma alteração" : " fez " + n + " alterações") + " no seu treino. Confira os exercícios.",
                    ultimo.getDataHora()));
        }
    }

    private void inatividade(Cliente aluno, LocalDate hoje, List<NotificacaoApp> lista) {
        if (exercicios.findByClienteId(aluno.getId()).isEmpty()) {
            return;
        }
        LocalDate ultimo = realizados.findFirstByClienteIdOrderByDataDesc(aluno.getId())
                .map(com.ccam.model.TreinoRealizado::getData)
                .orElse(aluno.getDataCadastro() == null ? null : aluno.getDataCadastro().toLocalDate());
        if (ultimo == null) {
            return;
        }
        long dias = ChronoUnit.DAYS.between(ultimo, hoje);
        if (dias < DIAS_SEM_TREINO) {
            return;
        }
        long ciclo = (dias - DIAS_SEM_TREINO) / 5;
        lista.add(new NotificacaoApp("inativo:" + ultimo + ":" + ciclo, "treino", "Sentimos sua falta!",
                "Faz " + dias + " dias que você não registra um treino. Que tal voltar hoje?",
                ultimo.plusDays(DIAS_SEM_TREINO + ciclo * 5).atTime(10, 0)));
    }

    private static String resumo(String texto) {
        if (texto == null) {
            return "";
        }
        String t = texto.strip();
        return t.length() > 140 ? t.substring(0, 137).strip() + "..." : t;
    }
}
