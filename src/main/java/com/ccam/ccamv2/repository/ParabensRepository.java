package com.ccam.ccamv2.repository;

import com.ccam.ccamv2.model.Parabens;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ParabensRepository extends JpaRepository<Parabens, UUID> {
    boolean existsByRemetenteIdAndDestinatarioIdAndAno(UUID remetenteId, UUID destinatarioId, int ano);

    List<Parabens> findByRemetenteIdAndAno(UUID remetenteId, int ano);

    List<Parabens> findTop50ByDestinatarioIdAndCriadoEmAfterOrderByCriadoEmDesc(UUID destinatarioId, LocalDateTime desde);

    long countByDestinatarioIdAndLidoFalse(UUID destinatarioId);

    @Modifying
    @Query("update Parabens p set p.lido = true where p.destinatarioId = :destinatarioId and p.lido = false")
    int marcarLidos(@Param("destinatarioId") UUID destinatarioId);
}
