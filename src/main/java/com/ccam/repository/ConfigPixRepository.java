package com.ccam.repository;

import com.ccam.model.ConfigPix;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ConfigPixRepository extends JpaRepository<ConfigPix, UUID> {
}
