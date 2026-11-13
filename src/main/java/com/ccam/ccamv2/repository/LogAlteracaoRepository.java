package com.ccam.ccamv2.repository;

import com.ccam.ccamv2.model.LogAlteracao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LogAlteracaoRepository extends JpaRepository<LogAlteracao, UUID> {
    Page<LogAlteracao> findByEmpresaIdAndAlunoId(UUID empresaId, UUID alunoId, Pageable pageable);
}
