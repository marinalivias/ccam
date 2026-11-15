package com.ccam.repository;

import com.ccam.model.CatalogoExercicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CatalogoExercicioRepository extends JpaRepository<CatalogoExercicio, Long> {
    java.util.Optional<CatalogoExercicio> findFirstByNome(String nome);
}
