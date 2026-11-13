package com.ccam.ccamv2.controller;

import com.ccam.ccamv2.dto.PaginaResposta;
import com.ccam.ccamv2.exception.NegocioException;
import com.ccam.ccamv2.model.Mural;
import com.ccam.ccamv2.model.Usuario;
import com.ccam.ccamv2.repository.MuralRepository;
import com.ccam.ccamv2.security.AuthInterceptor;
import com.ccam.ccamv2.security.Permissao;
import com.ccam.ccamv2.security.RequerPermissao;
import com.ccam.ccamv2.service.LogService;
import com.ccam.ccamv2.service.Paginacao;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/mural")
@CrossOrigin(origins = "*")
public class MuralController {
    private static final LocalDateTime INICIO = Paginacao.DATA_MINIMA.atStartOfDay();
    private static final LocalDateTime FIM = Paginacao.DATA_MAXIMA.atTime(LocalTime.MAX);

    private final MuralRepository repository;
    private final LogService logService;

    public MuralController(MuralRepository repository, LogService logService) {
        this.repository = repository;
        this.logService = logService;
    }

    @GetMapping
    @RequerPermissao(Permissao.MURAL_VER)
    public ResponseEntity<PaginaResposta<Mural>> listarMural(
            @RequestAttribute(AuthInterceptor.ATRIBUTO) Usuario usuario,
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) String mes,
            @RequestParam(required = false) Integer pagina,
            @RequestParam(required = false) Integer tamanho) {
        YearMonth m = Paginacao.mes(mes);
        LocalDateTime inicio = m != null ? m.atDay(1).atStartOfDay() : INICIO;
        LocalDateTime fim = m != null ? m.atEndOfMonth().atTime(LocalTime.MAX) : FIM;
        var page = repository.buscar(usuario.getEmpresaId(), inicio, fim, Paginacao.like(Paginacao.termo(busca)),
                Paginacao.criar(pagina, tamanho, Sort.by(Sort.Direction.DESC, "dataPublicacao")));
        return ResponseEntity.ok(PaginaResposta.de(page));
    }

    @PostMapping
    @RequerPermissao(Permissao.MURAL_PUBLICAR)
    @Transactional
    public ResponseEntity<Mural> publicarAviso(@RequestAttribute(AuthInterceptor.ATRIBUTO) Usuario usuario,
                                               @RequestBody Mural aviso) {
        validar(aviso);
        aviso.setId(null);
        aviso.setEmpresaId(usuario.getEmpresaId());
        aviso.setDataPublicacao(LocalDateTime.now());
        aviso.setDataAtualizacao(null);
        aviso.setAutorNome(usuario.getNome());
        Mural salvo = repository.save(aviso);
        logService.registrar(usuario, LogService.AVISO, salvo.getId(), null, LogService.CRIACAO,
                "Aviso publicado: " + salvo.getTitulo());
        return ResponseEntity.ok(salvo);
    }

    @PutMapping("/{id}")
    @RequerPermissao(Permissao.MURAL_PUBLICAR)
    @Transactional
    public ResponseEntity<Mural> editarAviso(@RequestAttribute(AuthInterceptor.ATRIBUTO) Usuario usuario,
                                             @PathVariable UUID id,
                                             @RequestBody Mural dados) {
        Mural atual = daEmpresa(usuario, id);
        validar(dados);
        LogService.Diferencas dif = new LogService.Diferencas()
                .campo("Título", atual.getTitulo(), dados.getTitulo().trim())
                .sensivel("Mensagem", atual.getMensagem(), dados.getMensagem());
        if (dif.vazio()) {
            return ResponseEntity.ok(atual);
        }
        atual.setTitulo(dados.getTitulo().trim());
        atual.setMensagem(dados.getMensagem());
        atual.setDataAtualizacao(LocalDateTime.now());
        Mural salvo = repository.save(atual);
        logService.registrar(usuario, LogService.AVISO, salvo.getId(), null, LogService.EDICAO, dif.toString());
        return ResponseEntity.ok(salvo);
    }

    @DeleteMapping("/{id}")
    @RequerPermissao(Permissao.MURAL_PUBLICAR)
    @Transactional
    public ResponseEntity<Map<String, String>> apagarAviso(@RequestAttribute(AuthInterceptor.ATRIBUTO) Usuario usuario,
                                                           @PathVariable UUID id) {
        Mural atual = daEmpresa(usuario, id);
        repository.delete(atual);
        logService.registrar(usuario, LogService.AVISO, atual.getId(), null, LogService.EXCLUSAO,
                "Aviso apagado: " + atual.getTitulo());
        return ResponseEntity.ok(Map.of("mensagem", "Aviso apagado."));
    }

    private Mural daEmpresa(Usuario usuario, UUID id) {
        return repository.findById(id)
                .filter(m -> usuario.getEmpresaId() != null && usuario.getEmpresaId().equals(m.getEmpresaId()))
                .orElseThrow(() -> new NegocioException("Aviso não encontrado."));
    }

    private void validar(Mural aviso) {
        if (aviso.getTitulo() == null || aviso.getTitulo().isBlank()) {
            throw new NegocioException("Informe o título do aviso.");
        }
        if (aviso.getTitulo().trim().length() > 255) {
            throw new NegocioException("O título pode ter no máximo 255 caracteres.");
        }
    }
}
