package com.ccam.ccamv2.controller;

import com.ccam.ccamv2.exception.NegocioException;
import com.ccam.ccamv2.model.CatalogoExercicio;
import com.ccam.ccamv2.repository.CatalogoExercicioRepository;
import com.ccam.ccamv2.repository.ExercicioRepository;
import com.ccam.ccamv2.security.Permissao;
import com.ccam.ccamv2.security.RequerPermissao;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/catalogo")
@CrossOrigin(origins = "*")
public class CatalogoExercicioController {
    private final CatalogoExercicioRepository repository;
    private final ExercicioRepository exercicios;

    public CatalogoExercicioController(CatalogoExercicioRepository repository, ExercicioRepository exercicios) {
        this.repository = repository;
        this.exercicios = exercicios;
    }

    @GetMapping
    @RequerPermissao(Permissao.TREINOS_VER)
    public List<CatalogoExercicio> listarCatalogo() {
        return repository.findAll(Sort.by("grupoMuscular", "nome"));
    }

    @PutMapping("/{id}")
    @RequerPermissao(Permissao.TREINOS_EDITAR)
    @Transactional
    public CatalogoExercicio editar(@PathVariable Long id, @RequestBody Map<String, String> dados) {
        CatalogoExercicio item = repository.findById(id)
                .orElseThrow(() -> new NegocioException("Exercício não encontrado."));
        String url = limpo(dados.get("imagemUrl"));
        if (url != null && !(url.startsWith("http://") || url.startsWith("https://") || url.startsWith("/"))) {
            throw new NegocioException("O link do GIF deve começar com http:// ou https://");
        }
        String antiga = item.getImagemUrl();
        item.setImagemUrl(url != null && url.length() > 500 ? url.substring(0, 500) : url);
        String instr = limpo(dados.get("instrucoes"));
        item.setInstrucoes(instr != null && instr.length() > 3000 ? instr.substring(0, 3000) : instr);
        CatalogoExercicio salvo = repository.save(item);
        exercicios.atualizarImagem(salvo.getNome(), antiga == null ? "" : antiga, salvo.getImagemUrl());
        return salvo;
    }

    private static String limpo(String v) {
        if (v == null) {
            return null;
        }
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }
}
