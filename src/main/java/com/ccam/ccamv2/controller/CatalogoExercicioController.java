package com.ccam.ccamv2.controller;

import com.ccam.ccamv2.model.CatalogoExercicio;
import com.ccam.ccamv2.repository.CatalogoExercicioRepository;
import com.ccam.ccamv2.security.Permissao;
import com.ccam.ccamv2.security.RequerPermissao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalogo")
@CrossOrigin(origins = "*")
public class CatalogoExercicioController {
    @Autowired
    private CatalogoExercicioRepository repository;

    @GetMapping
    @RequerPermissao(Permissao.TREINOS_VER)
    public List<CatalogoExercicio> listarCatalogo() {
        return repository.findAll();
    }
}
