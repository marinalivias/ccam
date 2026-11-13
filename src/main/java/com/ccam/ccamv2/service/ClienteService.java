package com.ccam.ccamv2.service;

import com.ccam.ccamv2.exception.NegocioException;
import com.ccam.ccamv2.model.Cliente;
import com.ccam.ccamv2.model.Usuario;
import com.ccam.ccamv2.repository.ClienteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class ClienteService {
    private static final Set<String> STATUS = Set.of("ativos", "inativos", "todos");
    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ClienteRepository repository;
    private final LogService logService;

    public ClienteService(ClienteRepository repository, LogService logService) {
        this.repository = repository;
        this.logService = logService;
    }

    private String soNumeros(String valor) {
        return valor == null ? "" : valor.replaceAll("[^0-9]", "");
    }

    private void validar(Cliente cliente) {
        if (cliente.getNome() == null || cliente.getNome().isBlank()) {
            throw new NegocioException("Informe o nome do aluno.");
        }
        if (cliente.getEmail() == null || cliente.getEmail().isBlank()) {
            throw new NegocioException("Informe o e-mail do aluno.");
        }
        if (cliente.getCpf() == null || cliente.getCpf().isBlank()) {
            throw new NegocioException("Informe o CPF do aluno.");
        }
        cliente.setNome(cliente.getNome().trim());
        cliente.setEmail(cliente.getEmail().trim().toLowerCase());
        cliente.setCpf(soNumeros(cliente.getCpf()));
        if (cliente.getCpf().length() != 11) {
            throw new NegocioException("O CPF deve ter 11 números.");
        }
    }

    private void verificarDuplicidade(Cliente cliente, UUID ignorarId) {
        repository.findByEmailIgnoreCase(cliente.getEmail())
                .filter(c -> !c.getId().equals(ignorarId))
                .ifPresent(c -> { throw new NegocioException("E-mail já cadastrado!"); });
        repository.findByCpf(cliente.getCpf())
                .filter(c -> !c.getId().equals(ignorarId))
                .ifPresent(c -> { throw new NegocioException("CPF já cadastrado!"); });
    }

    public Cliente salvar(Cliente cliente) {
        validar(cliente);
        verificarDuplicidade(cliente, null);
        cliente.setAtivo(true);
        cliente.setDataCadastro(LocalDateTime.now());
        return repository.save(cliente);
    }

    @Transactional
    public Cliente cadastrar(Usuario autor, Cliente cliente) {
        cliente.setId(null);
        cliente.setEmpresaId(autor.getEmpresaId());
        Cliente novo = salvar(cliente);
        logService.registrar(autor, LogService.ALUNO, novo.getId(), novo.getId(), LogService.CRIACAO,
                "Aluno cadastrado: " + novo.getNome());
        return novo;
    }

    @Transactional
    public Cliente atualizar(Usuario autor, UUID id, Cliente dados) {
        Cliente atual = buscarDaEmpresa(id, autor.getEmpresaId())
                .orElseThrow(() -> new NegocioException("Aluno não encontrado."));
        validar(dados);
        verificarDuplicidade(dados, atual.getId());

        LogService.Diferencas dif = new LogService.Diferencas()
                .campo("Nome", atual.getNome(), dados.getNome())
                .campo("E-mail", atual.getEmail(), dados.getEmail())
                .campo("CPF", atual.getCpf(), dados.getCpf())
                .campo("Celular", atual.getTelefone(), dados.getTelefone())
                .campo("Telefone fixo", atual.getTelefoneFixo(), dados.getTelefoneFixo())
                .campo("Data de nascimento", dataDe(atual), dataDe(dados))
                .campo("Sexo", atual.getSexo(), dados.getSexo())
                .campo("Peso (kg)", atual.getPeso(), dados.getPeso())
                .campo("Altura (m)", atual.getAltura(), dados.getAltura())
                .sensivel("Condição de saúde", atual.getCondicaoSaude(), dados.getCondicaoSaude());

        if (dif.vazio()) {
            return atual;
        }

        atual.setNome(dados.getNome());
        atual.setEmail(dados.getEmail());
        atual.setCpf(dados.getCpf());
        atual.setTelefone(dados.getTelefone());
        atual.setTelefoneFixo(dados.getTelefoneFixo());
        atual.setDataNascimento(dados.getDataNascimento());
        atual.setSexo(dados.getSexo());
        atual.setPeso(dados.getPeso());
        atual.setAltura(dados.getAltura());
        atual.setCondicaoSaude(dados.getCondicaoSaude());
        atual.setDataAtualizacao(LocalDateTime.now());
        Cliente salvo = repository.save(atual);

        logService.registrar(autor, LogService.ALUNO, salvo.getId(), salvo.getId(), LogService.EDICAO, dif.toString());
        return salvo;
    }

    @Transactional
    public Cliente alterarAtivo(Usuario autor, UUID id, boolean ativo) {
        Cliente c = buscarDaEmpresa(id, autor.getEmpresaId())
                .orElseThrow(() -> new NegocioException("Aluno não encontrado."));
        if (c.isAtivo() == ativo) {
            return c;
        }
        c.setAtivo(ativo);
        c.setDataAtualizacao(LocalDateTime.now());
        Cliente salvo = repository.save(c);
        logService.registrar(autor, LogService.ALUNO, salvo.getId(), salvo.getId(),
                ativo ? LogService.REATIVACAO : LogService.INATIVACAO,
                ativo ? "Aluno reativado" : "Aluno inativado");
        return salvo;
    }

    public Page<Cliente> buscar(UUID empresaId, String busca, String status, Pageable pageable) {
        String s = status != null && STATUS.contains(status) ? status : "ativos";
        String termo = Paginacao.termo(busca);
        String digitos = soNumeros(termo);
        return repository.buscar(empresaId, s, Paginacao.like(termo), Paginacao.like(digitos), pageable);
    }

    public Cliente fazerLogin(String email, String cpf) {
        String cpfDigitado = soNumeros(cpf);
        Cliente cliente = repository.findByEmailIgnoreCase(email == null ? "" : email.trim())
                .filter(c -> soNumeros(c.getCpf()).equals(cpfDigitado))
                .orElseThrow(() -> new RuntimeException("E-mail ou senha (CPF) incorretos."));
        if (!cliente.isAtivo()) {
            throw new RuntimeException("Seu cadastro está inativo. Procure a recepção da academia.");
        }
        return cliente;
    }

    public List<Cliente> listarTodos() {
        return repository.findAll();
    }

    public List<Cliente> listarPorEmpresa(UUID empresaId) {
        return repository.findByEmpresaIdOrderByNomeAsc(empresaId);
    }

    public List<Cliente> listarAtivos(UUID empresaId) {
        return repository.findByEmpresaIdAndAtivoTrueOrderByNomeAsc(empresaId);
    }

    public Optional<Cliente> buscarDaEmpresa(UUID clienteId, UUID empresaId) {
        if (clienteId == null || empresaId == null) {
            return Optional.empty();
        }
        return repository.findById(clienteId).filter(c -> empresaId.equals(c.getEmpresaId()));
    }

    private static String dataDe(Cliente c) {
        return c.getDataNascimento() == null ? null : c.getDataNascimento().format(DATA_BR);
    }
}
