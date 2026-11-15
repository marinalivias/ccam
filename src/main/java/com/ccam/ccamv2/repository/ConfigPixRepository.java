package com.ccam.ccamv2.repository;

import com.ccam.ccamv2.model.ConfigPix;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ConfigPixRepository extends JpaRepository<ConfigPix, UUID> {
}
