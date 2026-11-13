package com.ccam.ccamv2.repository;

import com.ccam.ccamv2.model.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProdutoRepository extends JpaRepository<Produto, UUID> {
    List<Produto> findByEmpresaIdAndAtivoTrueOrderByCategoriaAscNomeAsc(UUID empresaId);

    @Query("""
            select p from Produto p
            where p.empresaId = :empresaId
              and (:status = 'todos'
                   or (:status = 'visiveis' and p.ativo = true)
                   or (:status = 'ocultos' and p.ativo = false))
              and (:like = ''
                   or lower(p.nome) like :like
                   or lower(coalesce(p.categoria, '')) like :like)
            """)
    Page<Produto> buscar(@Param("empresaId") UUID empresaId,
                         @Param("status") String status,
                         @Param("like") String like,
                         Pageable pageable);
}
