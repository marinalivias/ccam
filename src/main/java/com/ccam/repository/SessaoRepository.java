package com.ccam.repository;

import com.ccam.model.Sessao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.UUID;

public interface SessaoRepository extends JpaRepository<Sessao, String> {
    void deleteByUsuarioId(UUID usuarioId);

    void deleteByExpiraEmBefore(LocalDateTime limite);
}
