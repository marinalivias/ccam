package com.ccam.ccamv2.repository;

import com.ccam.ccamv2.model.Exercicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface ExercicioRepository extends JpaRepository<Exercicio, UUID> {
    List<Exercicio> findByClienteIdAndDiaSemana(UUID clienteId, String diaSemana);

    List<Exercicio> findByClienteId(UUID clienteId);

    @Modifying
    @Query("update Exercicio e set e.imagemUrl = :nova where e.nomeExercicio = :nome and (e.imagemUrl is null or e.imagemUrl = :antiga)")
    int atualizarImagem(@Param("nome") String nome, @Param("antiga") String antiga, @Param("nova") String nova);
}
