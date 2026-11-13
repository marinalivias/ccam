package com.ccam.ccamv2.repository;

import com.ccam.ccamv2.model.CatalogoExercicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CatalogoExercicioRepository extends JpaRepository<CatalogoExercicio, Long> {
}
