package com.ccam.ccamv2.repository;

import com.ccam.ccamv2.model.ConfigLoja;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ConfigLojaRepository extends JpaRepository<ConfigLoja, UUID> {
}
