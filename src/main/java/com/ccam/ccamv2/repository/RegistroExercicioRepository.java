package com.ccam.ccamv2.repository;

import com.ccam.ccamv2.model.RegistroExercicio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RegistroExercicioRepository extends JpaRepository<RegistroExercicio, UUID> {
    List<RegistroExercicio> findByClienteIdAndData(UUID clienteId, LocalDate data);

    List<RegistroExercicio> findByClienteIdAndDataBetween(UUID clienteId, LocalDate inicio, LocalDate fim);

    Optional<RegistroExercicio> findByClienteIdAndDataAndExercicioId(UUID clienteId, LocalDate data, UUID exercicioId);
}
