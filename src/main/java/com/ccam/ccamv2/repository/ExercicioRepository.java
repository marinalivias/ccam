package com.ccam.ccamv2.repository;

import com.ccam.ccamv2.model.Exercicio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ExercicioRepository extends JpaRepository<Exercicio, UUID> {
    List<Exercicio> findByClienteIdAndDiaSemana(UUID clienteId, String diaSemana);

    List<Exercicio> findByClienteId(UUID clienteId);
}
