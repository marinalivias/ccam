package com.ccam.ccamv2.repository;

import com.ccam.ccamv2.model.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClienteRepository extends JpaRepository<Cliente, UUID> {
    Optional<Cliente> findByEmailAndCpf(String email, String cpf);

    Optional<Cliente> findByEmail(String email);

    Optional<Cliente> findByEmailIgnoreCase(String email);

    Optional<Cliente> findByCpf(String cpf);

    List<Cliente> findByEmpresaIdOrderByNomeAsc(UUID empresaId);

    List<Cliente> findByEmpresaIdAndAtivoTrueOrderByNomeAsc(UUID empresaId);

    @Query("""
            select c from Cliente c
            where c.empresaId = :empresaId
              and (:status = 'todos'
                   or (:status = 'ativos' and c.ativo = true)
                   or (:status = 'inativos' and c.ativo = false))
              and (:like = ''
                   or lower(c.nome) like :like
                   or lower(c.email) like :like
                   or (:cpfLike <> '' and c.cpf like :cpfLike))
            """)
    Page<Cliente> buscar(@Param("empresaId") UUID empresaId,
                         @Param("status") String status,
                         @Param("like") String like,
                         @Param("cpfLike") String cpfLike,
                         Pageable pageable);

    long countByEmpresaIdAndAtivoTrue(UUID empresaId);
}
