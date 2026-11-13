package com.ccam.ccamv2.repository;

import com.ccam.ccamv2.model.Financeiro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface FinanceiroRepository extends JpaRepository<Financeiro, UUID> {
    List<Financeiro> findByClienteIdOrderByVencimentoDesc(UUID clienteId);

    List<Financeiro> findByEmpresaIdOrderByVencimentoDesc(UUID empresaId);

    @Query("""
            select f from Financeiro f
            where f.empresaId = :empresaId
              and (:status = '' or f.status = :status)
              and f.vencimento between :inicio and :fim
              and (:like = '' or f.clienteId in (
                    select c.id from Cliente c
                    where c.empresaId = :empresaId and lower(c.nome) like :like))
            """)
    Page<Financeiro> buscar(@Param("empresaId") UUID empresaId,
                            @Param("status") String status,
                            @Param("inicio") LocalDate inicio,
                            @Param("fim") LocalDate fim,
                            @Param("like") String like,
                            Pageable pageable);
}
