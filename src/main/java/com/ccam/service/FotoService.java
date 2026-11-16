package com.ccam.service;

import com.ccam.exception.NegocioException;
import com.ccam.model.Cliente;
import com.ccam.model.FotoAluno;
import com.ccam.repository.ClienteRepository;
import com.ccam.repository.FotoAlunoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class FotoService {
    private static final int TAMANHO_MAXIMO = 400 * 1024;
    private static final Set<String> TIPOS = Set.of("image/jpeg", "image/png", "image/webp");

    private final FotoAlunoRepository fotos;
    private final ClienteRepository clientes;

    public FotoService(FotoAlunoRepository fotos, ClienteRepository clientes) {
        this.fotos = fotos;
        this.clientes = clientes;
    }

    @Transactional
    public Cliente salvar(Cliente aluno, String tipo, String base64) {
        if (tipo == null || !TIPOS.contains(tipo)) {
            throw new NegocioException("Use uma foto JPG, PNG ou WEBP.");
        }
        byte[] dados;
        try {
            dados = Base64.getDecoder().decode(base64 == null ? "" : base64);
        } catch (IllegalArgumentException e) {
            throw new NegocioException("Não foi possível ler a foto.");
        }
        if (dados.length == 0) {
            throw new NegocioException("Escolha uma foto.");
        }
        if (dados.length > TAMANHO_MAXIMO) {
            throw new NegocioException("A foto é grande demais. Escolha uma imagem menor.");
        }
        FotoAluno foto = fotos.findById(aluno.getId()).orElseGet(FotoAluno::new);
        foto.setClienteId(aluno.getId());
        foto.setTipo(tipo);
        foto.setDados(dados);
        foto.setAtualizadaEm(LocalDateTime.now());
        fotos.save(foto);
        aluno.setFotoAtualizadaEm(foto.getAtualizadaEm());
        return clientes.save(aluno);
    }

    @Transactional
    public Cliente remover(Cliente aluno) {
        fotos.deleteById(aluno.getId());
        aluno.setFotoAtualizadaEm(null);
        return clientes.save(aluno);
    }

    public Optional<Map<String, String>> buscar(Cliente aluno) {
        return fotos.findById(aluno.getId())
                .map(f -> Map.of("tipo", f.getTipo(), "base64", Base64.getEncoder().encodeToString(f.getDados())));
    }
}
