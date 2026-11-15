package com.ccam.repository;

import com.ccam.model.LogAlteracao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LogAlteracaoRepository extends JpaRepository<LogAlteracao, UUID> {
    Page<LogAlteracao> findByEmpresaIdAndAlunoId(UUID empresaId, UUID alunoId, Pageable pageable);

    java.util.List<LogAlteracao> findByAlunoIdAndEntidadeAndUsuarioIdIsNotNullAndDataHoraAfterOrderByDataHoraDesc(
            UUID alunoId, String entidade, java.time.LocalDateTime depois);
}
