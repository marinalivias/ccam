package com.ccam.ccamv2.repository;

import com.ccam.ccamv2.model.SessaoAluno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface SessaoAlunoRepository extends JpaRepository<SessaoAluno, String> {
    void deleteByExpiraEmBefore(LocalDateTime limite);
}
