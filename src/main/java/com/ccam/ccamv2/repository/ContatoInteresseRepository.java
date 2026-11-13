package com.ccam.ccamv2.repository;

import com.ccam.ccamv2.model.ContatoInteresse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ContatoInteresseRepository extends JpaRepository<ContatoInteresse, UUID> {
    @Query("""
            select c from ContatoInteresse c
            where (:status = 'todos'
                   or (:status = 'pendentes' and c.atendido = false)
                   or (:status = 'atendidos' and c.atendido = true))
              and (:like = ''
                   or lower(c.nome) like :like
                   or lower(c.email) like :like
                   or lower(coalesce(c.academia, '')) like :like
                   or lower(coalesce(c.cidade, '')) like :like)
            """)
    Page<ContatoInteresse> buscar(@Param("status") String status, @Param("like") String like, Pageable pageable);
}
