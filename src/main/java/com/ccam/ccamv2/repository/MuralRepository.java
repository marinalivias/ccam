package com.ccam.ccamv2.repository;

import com.ccam.ccamv2.model.Mural;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface MuralRepository extends JpaRepository<Mural, UUID> {
    List<Mural> findAllByOrderByDataPublicacaoDesc();

    List<Mural> findByEmpresaIdOrderByDataPublicacaoDesc(UUID empresaId);

    @Query("""
            select m from Mural m
            where m.empresaId = :empresaId
              and m.dataPublicacao between :inicio and :fim
              and (:like = ''
                   or lower(m.titulo) like :like
                   or lower(coalesce(m.mensagem, '')) like :like)
            """)
    Page<Mural> buscar(@Param("empresaId") UUID empresaId,
                       @Param("inicio") LocalDateTime inicio,
                       @Param("fim") LocalDateTime fim,
                       @Param("like") String like,
                       Pageable pageable);
}
