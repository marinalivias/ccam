package com.ccam.ccamv2.repository;

import com.ccam.ccamv2.model.TreinoRealizado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TreinoRealizadoRepository extends JpaRepository<TreinoRealizado, UUID> {
    Optional<TreinoRealizado> findByClienteIdAndData(UUID clienteId, LocalDate data);

    List<TreinoRealizado> findByClienteIdAndDataBetweenOrderByDataAsc(UUID clienteId, LocalDate inicio, LocalDate fim);

    Optional<TreinoRealizado> findFirstByClienteIdOrderByDataDesc(UUID clienteId);

    @Query("select count(distinct t.clienteId) from TreinoRealizado t where t.empresaId = :empresaId and t.data between :inicio and :fim")
    long contarAlunosComTreino(@Param("empresaId") UUID empresaId, @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("select t.clienteId, max(t.data) from TreinoRealizado t where t.empresaId = :empresaId group by t.clienteId")
    List<Object[]> ultimoTreinoPorAluno(@Param("empresaId") UUID empresaId);
}
