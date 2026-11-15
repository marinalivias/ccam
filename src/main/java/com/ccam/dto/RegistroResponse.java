package com.ccam.dto;

import com.ccam.model.RegistroExercicio;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public record RegistroResponse(UUID exercicioId, List<Integer> reps, String carga, boolean completo) {

    public static RegistroResponse de(RegistroExercicio r) {
        List<Integer> reps = r.getReps() == null || r.getReps().isBlank()
                ? List.of()
                : Arrays.stream(r.getReps().split(",")).map(String::trim).map(Integer::valueOf).toList();
        return new RegistroResponse(r.getExercicioId(), reps, r.getCarga(), r.isCompleto());
    }
}
