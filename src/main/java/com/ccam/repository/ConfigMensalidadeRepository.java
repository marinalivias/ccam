package com.ccam.repository;

import com.ccam.model.ConfigMensalidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ConfigMensalidadeRepository extends JpaRepository<ConfigMensalidade, UUID> {
    List<ConfigMensalidade> findByAtivaTrue();
}
