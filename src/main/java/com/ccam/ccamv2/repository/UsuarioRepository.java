package com.ccam.ccamv2.repository;

import com.ccam.ccamv2.model.Usuario;
import com.ccam.ccamv2.security.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByPerfil(Perfil perfil);

    List<Usuario> findByEmpresaIdOrderByNomeAsc(UUID empresaId);

    Optional<Usuario> findFirstByEmpresaIdAndTitularTrue(UUID empresaId);

    Optional<Usuario> findByResetTokenHash(String resetTokenHash);
}
