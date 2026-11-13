package com.ccam.ccamv2.service;

import com.ccam.ccamv2.dto.FrequenciaResponse;
import com.ccam.ccamv2.model.Cliente;
import com.ccam.ccamv2.model.Exercicio;
import com.ccam.ccamv2.model.TreinoRealizado;
import com.ccam.ccamv2.repository.ExercicioRepository;
import com.ccam.ccamv2.repository.TreinoRealizadoRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FrequenciaService {
    private static final int JANELA_DIAS = 28;
    private static final int DIAS_ROTATIVO = 3;

    private final ExercicioRepository exercicios;
    private final TreinoRealizadoRepository realizados;

    public FrequenciaService(ExercicioRepository exercicios, TreinoRealizadoRepository realizados) {
        this.exercicios = exercicios;
        this.realizados = realizados;
    }

    public int diasPorSemana(Cliente aluno) {
        List<Exercicio> ficha = exercicios.findByClienteId(aluno.getId());
        Set<String> dias = ficha.stream().map(Exercicio::getDiaSemana).collect(Collectors.toSet());
        long fixos = dias.stream().filter(DiasSemana.NOMES::contains).count();
        if (fixos > 0) {
            return (int) Math.min(7, fixos);
        }
        return dias.contains(DiasSemana.ROTATIVO) ? DIAS_ROTATIVO : 0;
    }

    public FrequenciaResponse calcular(Cliente aluno) {
        LocalDate hoje = LocalDate.now();
        int porSemana = diasPorSemana(aluno);

        LocalDate inicioJanela = hoje.minusDays(JANELA_DIAS - 1);
        if (aluno.getDataCadastro() != null && aluno.getDataCadastro().toLocalDate().isAfter(inicioJanela)) {
            inicioJanela = aluno.getDataCadastro().toLocalDate();
        }
        long diasJanela = ChronoUnit.DAYS.between(inicioJanela, hoje) + 1;

        List<TreinoRealizado> lista = realizados.findByClienteIdAndDataBetweenOrderByDataAsc(
                aluno.getId(), hoje.minusDays(400), hoje);
        Set<LocalDate> dias = lista.stream()
                .filter(t -> t.getExerciciosFeitos() > 0)
                .map(TreinoRealizado::getData)
                .collect(Collectors.toSet());

        final LocalDate inicio = inicioJanela;
        int treinosJanela = (int) dias.stream().filter(d -> !d.isBefore(inicio)).count();
        int previstos = porSemana == 0 ? 0 : (int) Math.max(1, Math.round(porSemana * diasJanela / 7.0));
        int percentual = previstos == 0 ? 0 : Math.min(100, Math.round(treinosJanela * 100f / previstos));

        LocalDate segunda = hoje.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        int semana = (int) dias.stream().filter(d -> !d.isBefore(segunda)).count();

        int seguidas = 0;
        if (porSemana > 0) {
            LocalDate s = semana >= porSemana ? segunda : segunda.minusWeeks(1);
            for (int i = 0; i < 52; i++) {
                LocalDate ini = s;
                LocalDate fim = s.plusDays(6);
                long qtd = dias.stream().filter(d -> !d.isBefore(ini) && !d.isAfter(fim)).count();
                if (qtd < porSemana) {
                    break;
                }
                seguidas++;
                s = s.minusWeeks(1);
            }
        }

        LocalDate ultimo = dias.stream().max(LocalDate::compareTo).orElse(null);
        return new FrequenciaResponse(porSemana, semana, treinosJanela, previstos, percentual, seguidas, ultimo);
    }
}
