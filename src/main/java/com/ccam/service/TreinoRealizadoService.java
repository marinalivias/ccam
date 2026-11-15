package com.ccam.service;

import com.ccam.dto.TreinoRealizadoResponse;
import com.ccam.exception.NegocioException;
import com.ccam.model.Cliente;
import com.ccam.model.Exercicio;
import com.ccam.model.RegistroExercicio;
import com.ccam.model.TreinoRealizado;
import com.ccam.repository.ExercicioRepository;
import com.ccam.repository.RegistroExercicioRepository;
import com.ccam.repository.TreinoRealizadoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class TreinoRealizadoService {
    private static final Pattern NUMERO = Pattern.compile("\\d+");
    private static final int MAX_SERIES = 20;

    private final ExercicioRepository exercicios;
    private final TreinoRealizadoRepository realizados;
    private final RegistroExercicioRepository registros;

    public TreinoRealizadoService(ExercicioRepository exercicios, TreinoRealizadoRepository realizados,
                                  RegistroExercicioRepository registros) {
        this.exercicios = exercicios;
        this.realizados = realizados;
        this.registros = registros;
    }

    public static int series(Exercicio ex) {
        Matcher m = NUMERO.matcher(ex.getSeries() == null ? "" : ex.getSeries());
        int n = m.find() ? Integer.parseInt(m.group()) : 1;
        return Math.max(1, Math.min(MAX_SERIES, n));
    }

    public static Integer alvoReps(Exercicio ex) {
        String r = ex.getRepeticoes() == null ? "" : ex.getRepeticoes().toLowerCase();
        if (r.contains("seg") || r.contains("min") || r.contains("\"")) {
            return null;
        }
        Matcher m = NUMERO.matcher(r);
        return m.find() ? Integer.parseInt(m.group()) : null;
    }

    public List<TreinoRealizadoResponse> listar(Cliente aluno, LocalDate de, LocalDate ate) {
        Map<LocalDate, List<RegistroExercicio>> porDia = registros.findByClienteIdAndDataBetween(aluno.getId(), de, ate)
                .stream().collect(Collectors.groupingBy(RegistroExercicio::getData));
        return realizados.findByClienteIdAndDataBetweenOrderByDataAsc(aluno.getId(), de, ate).stream()
                .map(t -> TreinoRealizadoResponse.de(t, porDia.getOrDefault(t.getData(), List.of())))
                .toList();
    }

    @Transactional
    public Optional<TreinoRealizadoResponse> registrar(Cliente aluno, LocalDate data, UUID exercicioId,
                                                       List<Integer> reps, String carga) {
        validarData(data);
        List<Exercicio> previstos = previstos(aluno, data);
        Exercicio ex = previstos.stream().filter(e -> e.getId().equals(exercicioId)).findFirst()
                .orElseThrow(() -> new NegocioException("Este exercício não está no treino desse dia."));
        aplicarRegistro(aluno, data, ex, reps, carga);
        return recalcular(aluno, data, previstos, null, false);
    }

    @Transactional
    public Optional<TreinoRealizadoResponse> marcarCompletos(Cliente aluno, LocalDate data, List<UUID> ids, String observacao) {
        validarData(data);
        List<Exercicio> previstos = previstos(aluno, data);
        Set<UUID> marcados = ids == null ? Set.of() : new HashSet<>(ids);
        Map<UUID, RegistroExercicio> atuais = registros.findByClienteIdAndData(aluno.getId(), data).stream()
                .collect(Collectors.toMap(RegistroExercicio::getExercicioId, r -> r, (a, b) -> a));
        for (Exercicio ex : previstos) {
            RegistroExercicio r = atuais.get(ex.getId());
            boolean completo = r != null && r.isCompleto();
            if (marcados.contains(ex.getId()) && !completo) {
                aplicarRegistro(aluno, data, ex, null, r != null ? r.getCarga() : ex.getCarga());
            } else if (!marcados.contains(ex.getId()) && completo) {
                registros.delete(r);
            }
        }
        registros.flush();
        return recalcular(aluno, data, previstos, observacao, true);
    }

    private void aplicarRegistro(Cliente aluno, LocalDate data, Exercicio ex, List<Integer> reps, String carga) {
        int n = series(ex);
        Integer alvo = alvoReps(ex);
        int meta = alvo == null ? 1 : alvo;
        List<Integer> valores = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            int v = reps == null ? meta : (i < reps.size() && reps.get(i) != null ? reps.get(i) : 0);
            valores.add(Math.max(0, Math.min(alvo == null ? 1 : 999, v)));
        }
        Optional<RegistroExercicio> existente = registros.findByClienteIdAndDataAndExercicioId(aluno.getId(), data, ex.getId());
        if (valores.stream().allMatch(v -> v == 0)) {
            existente.ifPresent(registros::delete);
            registros.flush();
            return;
        }
        RegistroExercicio r = existente.orElseGet(RegistroExercicio::new);
        r.setClienteId(aluno.getId());
        r.setData(data);
        r.setExercicioId(ex.getId());
        r.setNomeExercicio(ex.getNomeExercicio());
        r.setSeriesPrevistas(n);
        r.setReps(valores.stream().map(String::valueOf).collect(Collectors.joining(",")));
        String c = carga == null ? null : carga.trim();
        r.setCarga(c == null || c.isEmpty() ? null : (c.length() > 40 ? c.substring(0, 40) : c));
        r.setCompleto(valores.stream().allMatch(v -> v >= meta));
        r.setAtualizadoEm(LocalDateTime.now());
        registros.saveAndFlush(r);
    }

    private Optional<TreinoRealizadoResponse> recalcular(Cliente aluno, LocalDate data, List<Exercicio> previstos,
                                                         String observacao, boolean trocarObservacao) {
        Set<UUID> validos = previstos.stream().map(Exercicio::getId).collect(Collectors.toSet());
        List<RegistroExercicio> lista = registros.findByClienteIdAndData(aluno.getId(), data).stream()
                .filter(r -> validos.contains(r.getExercicioId())).toList();
        Optional<TreinoRealizado> existente = realizados.findByClienteIdAndData(aluno.getId(), data);
        if (lista.isEmpty()) {
            existente.ifPresent(realizados::delete);
            return Optional.empty();
        }

        int seriesPrevistas = previstos.stream().mapToInt(TreinoRealizadoService::series).sum();
        Map<UUID, Exercicio> porId = previstos.stream().collect(Collectors.toMap(Exercicio::getId, e -> e));
        int seriesFeitas = 0;
        for (RegistroExercicio r : lista) {
            Integer alvo = alvoReps(porId.get(r.getExercicioId()));
            int meta = alvo == null ? 1 : alvo;
            for (String v : r.getReps().split(",")) {
                if (Integer.parseInt(v.trim()) >= meta) {
                    seriesFeitas++;
                }
            }
        }
        List<UUID> completos = lista.stream().filter(RegistroExercicio::isCompleto).map(RegistroExercicio::getExercicioId).toList();

        TreinoRealizado t = existente.orElseGet(TreinoRealizado::new);
        t.setEmpresaId(aluno.getEmpresaId());
        t.setClienteId(aluno.getId());
        t.setData(data);
        t.setDiaSemana(DiasSemana.de(data));
        t.setExerciciosPrevistos(previstos.size());
        t.setExerciciosFeitos(lista.size());
        t.setSeriesPrevistas(seriesPrevistas);
        t.setSeriesFeitas(seriesFeitas);
        t.setIdsFeitos(completos.stream().map(UUID::toString).collect(Collectors.joining(",")));
        t.setConcluido(completos.size() >= previstos.size());
        if (trocarObservacao) {
            String obs = observacao == null ? null : observacao.trim();
            t.setObservacao(obs == null || obs.isEmpty() ? null : (obs.length() > 500 ? obs.substring(0, 500) : obs));
        }
        t.setAtualizadoEm(LocalDateTime.now());
        return Optional.of(TreinoRealizadoResponse.de(realizados.save(t), lista));
    }

    private List<Exercicio> previstos(Cliente aluno, LocalDate data) {
        String dia = DiasSemana.de(data);
        return exercicios.findByClienteId(aluno.getId()).stream()
                .filter(e -> dia.equals(e.getDiaSemana()) || DiasSemana.ROTATIVO.equals(e.getDiaSemana()))
                .toList();
    }

    private static void validarData(LocalDate data) {
        LocalDate hoje = LocalDate.now();
        if (data.isAfter(hoje)) {
            throw new NegocioException("Não dá para marcar um treino no futuro.");
        }
        if (data.isBefore(hoje.minusDays(30))) {
            throw new NegocioException("Só dá para marcar treinos dos últimos 30 dias.");
        }
    }
}
