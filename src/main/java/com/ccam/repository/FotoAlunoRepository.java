package com.ccam.repository;

import com.ccam.model.FotoAluno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FotoAlunoRepository extends JpaRepository<FotoAluno, UUID> {
}
