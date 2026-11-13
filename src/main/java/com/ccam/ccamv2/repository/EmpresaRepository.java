package com.ccam.ccamv2.repository;

import com.ccam.ccamv2.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmpresaRepository extends JpaRepository<Empresa, UUID> {
    Optional<Empresa> findByCnpj(String cnpj);

    boolean existsByCnpj(String cnpj);

    List<Empresa> findAllByOrderByRazaoSocialAsc();
}
