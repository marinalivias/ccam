package com.ccam.repository;

import com.ccam.model.ConfigLoja;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ConfigLojaRepository extends JpaRepository<ConfigLoja, UUID> {
}
