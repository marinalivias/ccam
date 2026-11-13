package com.ccam.ccamv2.controller;

import com.ccam.ccamv2.exception.NegocioException;
import com.ccam.ccamv2.model.Cliente;
import com.ccam.ccamv2.model.Exercicio;
import com.ccam.ccamv2.model.Usuario;
import com.ccam.ccamv2.repository.ExercicioRepository;
import com.ccam.ccamv2.security.AuthInterceptor;
import com.ccam.ccamv2.security.Permissao;
import com.ccam.ccamv2.security.RequerPermissao;
import com.ccam.ccamv2.service.ClienteService;
import com.ccam.ccamv2.service.LogService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/exercicios")
@CrossOrigin(origins = "*")
public class ExercicioController {
    private final ExercicioRepository repository;
    private final ClienteService clienteService;
    private final LogService logService;

    public ExercicioController(ExercicioRepository repository, ClienteService clienteService, LogService logService) {
        this.repository = repository;
        this.clienteService = clienteService;
        this.logService = logService;
    }

    @GetMapping("/cliente/{clienteId}/dia/{diaSemana}")
    public ResponseEntity<List<Exercicio>> buscarTreinoDoDia(@PathVariable UUID clienteId, @PathVariable String diaSemana) {
        return ResponseEntity.ok(repository.findByClienteIdAndDiaSemana(clienteId, diaSemana));
    }

    @GetMapping("/aluno/{clienteId}")
    @RequerPermissao(Permissao.TREINOS_VER)
    public ResponseEntity<List<Exercicio>> buscarFicha(@RequestAttribute(AuthInterceptor.ATRIBUTO) Usuario usuario,
                                                       @PathVariable UUID clienteId) {
        aluno(usuario, clienteId);
        return ResponseEntity.ok(repository.findByClienteId(clienteId));
    }

    @PostMapping
    @RequerPermissao(Permissao.TREINOS_EDITAR)
    @Transactional
    public ResponseEntity<Exercicio> adicionarExercicio(@RequestAttribute(AuthInterceptor.ATRIBUTO) Usuario usuario,
                                                        @RequestBody Exercicio exercicio) {
        Cliente aluno = aluno(usuario, exercicio.getClienteId());
        if (!aluno.isAtivo()) {
            throw new NegocioException("Este aluno está inativo. Reative o cadastro antes de montar a ficha.");
        }
        validar(exercicio);
        exercicio.setId(null);
        Exercicio salvo = repository.save(exercicio);
        logService.registrar(usuario, LogService.TREINO, salvo.getId(), salvo.getClienteId(), LogService.CRIACAO,
                "Exercício adicionado à ficha: " + resumo(salvo));
        return ResponseEntity.ok(salvo);
    }

    @PutMapping("/{id}")
    @RequerPermissao(Permissao.TREINOS_EDITAR)
    @Transactional
    public ResponseEntity<Exercicio> editar(@RequestAttribute(AuthInterceptor.ATRIBUTO) Usuario usuario,
                                            @PathVariable UUID id,
                                            @RequestBody Exercicio dados) {
        Exercicio atual = doUsuario(usuario, id);
        validar(dados);

        LogService.Diferencas dif = new LogService.Diferencas()
                .campo("Dia", atual.getDiaSemana(), dados.getDiaSemana())
                .campo("Séries", atual.getSeries(), dados.getSeries())
                .campo("Repetições", atual.getRepeticoes(), dados.getRepeticoes())
                .campo("Carga", atual.getCarga(), dados.getCarga());
        if (dif.vazio()) {
            return ResponseEntity.ok(atual);
        }

        atual.setDiaSemana(dados.getDiaSemana());
        atual.setSeries(dados.getSeries());
        atual.setRepeticoes(dados.getRepeticoes());
        atual.setCarga(dados.getCarga());
        Exercicio salvo = repository.save(atual);
        logService.registrar(usuario, LogService.TREINO, salvo.getId(), salvo.getClienteId(), LogService.EDICAO,
                salvo.getNomeExercicio() + "\n" + dif);
        return ResponseEntity.ok(salvo);
    }

    @DeleteMapping("/{id}")
    @RequerPermissao(Permissao.TREINOS_EDITAR)
    @Transactional
    public ResponseEntity<Map<String, String>> remover(@RequestAttribute(AuthInterceptor.ATRIBUTO) Usuario usuario,
                                                       @PathVariable UUID id) {
        Exercicio atual = doUsuario(usuario, id);
        repository.delete(atual);
        logService.registrar(usuario, LogService.TREINO, atual.getId(), atual.getClienteId(), LogService.EXCLUSAO,
                "Exercício removido da ficha: " + resumo(atual));
        return ResponseEntity.ok(Map.of("mensagem", "Exercício removido da ficha."));
    }

    private Cliente aluno(Usuario usuario, UUID clienteId) {
        return clienteService.buscarDaEmpresa(clienteId, usuario.getEmpresaId())
                .orElseThrow(() -> new NegocioException("Aluno não encontrado."));
    }

    private Exercicio doUsuario(Usuario usuario, UUID id) {
        Exercicio ex = repository.findById(id)
                .orElseThrow(() -> new NegocioException("Exercício não encontrado."));
        aluno(usuario, ex.getClienteId());
        return ex;
    }

    private void validar(Exercicio ex) {
        if (ex.getDiaSemana() == null || ex.getDiaSemana().isBlank()) {
            throw new NegocioException("Informe o dia da semana.");
        }
        if (ex.getSeries() == null || ex.getSeries().isBlank()) {
            throw new NegocioException("Informe o número de séries.");
        }
        if (ex.getRepeticoes() == null || ex.getRepeticoes().isBlank()) {
            throw new NegocioException("Informe as repetições.");
        }
    }

    private static String resumo(Exercicio ex) {
        return ex.getNomeExercicio() + " (" + ex.getDiaSemana() + ", " + ex.getSeries() + " x "
                + ex.getRepeticoes() + (ex.getCarga() != null && !ex.getCarga().isBlank() ? ", " + ex.getCarga() : "") + ")";
    }
}
