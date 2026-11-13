package com.ccam.ccamv2.service;

import com.ccam.ccamv2.dto.EmpresaDados;
import com.ccam.ccamv2.dto.EmpresaItem;
import com.ccam.ccamv2.dto.NovaEmpresaRequest;
import com.ccam.ccamv2.exception.NegocioException;
import com.ccam.ccamv2.model.Empresa;
import com.ccam.ccamv2.model.Usuario;
import com.ccam.ccamv2.repository.EmpresaRepository;
import com.ccam.ccamv2.repository.UsuarioRepository;
import com.ccam.ccamv2.security.ValidadorCnpj;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class EmpresaService {
    private final EmpresaRepository empresas;
    private final UsuarioRepository usuarios;
    private final UsuarioService usuarioService;

    public EmpresaService(EmpresaRepository empresas, UsuarioRepository usuarios, UsuarioService usuarioService) {
        this.empresas = empresas;
        this.usuarios = usuarios;
        this.usuarioService = usuarioService;
    }

    public List<EmpresaItem> listar() {
        return empresas.findAllByOrderByRazaoSocialAsc().stream().map(this::item).toList();
    }

    public Empresa buscar(UUID id) {
        return empresas.findById(id).orElseThrow(() -> new NegocioException("Empresa não encontrada."));
    }

    @Transactional
    public EmpresaItem criar(NovaEmpresaRequest req) {
        if (req == null || req.empresa() == null || req.titular() == null) {
            throw new NegocioException("Informe os dados da empresa e do usuário titular.");
        }
        EmpresaDados d = req.empresa();
        String cnpj = Texto.soNumeros(d.cnpj());
        if (!ValidadorCnpj.valido(cnpj)) {
            throw new NegocioException("CNPJ inválido. Confira os números digitados.");
        }
        if (empresas.existsByCnpj(cnpj)) {
            throw new NegocioException("Já existe uma empresa cadastrada com este CNPJ.");
        }

        Empresa empresa = new Empresa();
        preencher(empresa, d);
        empresa.setCnpj(cnpj);
        empresa.setAtivo(true);
        empresa = empresas.save(empresa);

        usuarioService.criarTitular(empresa.getId(), req.titular());
        return item(empresa);
    }

    @Transactional
    public EmpresaItem atualizar(UUID id, EmpresaDados d) {
        Empresa empresa = buscar(id);
        preencher(empresa, d);

        String novoCnpj = Texto.soNumeros(d.cnpj());
        if (!novoCnpj.equals(empresa.getCnpj())) {
            if (!ValidadorCnpj.valido(novoCnpj)) {
                throw new NegocioException("CNPJ inválido. Confira os números digitados.");
            }
            if (empresas.existsByCnpj(novoCnpj)) {
                throw new NegocioException("Já existe uma empresa cadastrada com este CNPJ.");
            }
            empresa.setCnpj(novoCnpj);
        }
        return item(empresas.save(empresa));
    }

    @Transactional
    public Empresa atualizarMinha(UUID empresaId, EmpresaDados d) {
        Empresa empresa = buscar(empresaId);
        preencher(empresa, d);
        return empresas.save(empresa);
    }

    @Transactional
    public EmpresaItem alterarAtivo(UUID id, boolean ativo) {
        Empresa empresa = buscar(id);
        empresa.setAtivo(ativo);
        return item(empresas.save(empresa));
    }

    private EmpresaItem item(Empresa e) {
        Usuario titular = usuarios.findFirstByEmpresaIdAndTitularTrue(e.getId()).orElse(null);
        return new EmpresaItem(e,
                titular == null ? null : titular.getNome(),
                titular == null ? null : titular.getEmail());
    }

    private void preencher(Empresa e, EmpresaDados d) {
        String razao = Texto.limpo(d.razaoSocial());
        if (razao == null) {
            throw new NegocioException("Informe a razão social da empresa.");
        }
        e.setRazaoSocial(razao);
        e.setNomeFantasia(Texto.limpo(d.nomeFantasia()));
        e.setInscricaoEstadual(Texto.limpo(d.inscricaoEstadual()));
        e.setDataFundacao(d.dataFundacao());
        e.setProprietario(Texto.limpo(d.proprietario()));
        e.setAreasAtuacao(Texto.limpo(d.areasAtuacao()));

        String email = Texto.limpo(d.email());
        if (email != null) {
            email = email.toLowerCase();
            if (!Texto.emailValido(email)) {
                throw new NegocioException("O e-mail da empresa é inválido.");
            }
        }
        e.setEmail(email);
        e.setTelefone(Texto.limpo(d.telefone()));
        e.setWhatsapp(Texto.limpo(d.whatsapp()));
        e.setSite(Texto.limpo(d.site()));
        e.setInstagram(Texto.limpo(d.instagram()));
        e.setHorarioFuncionamento(Texto.limpo(d.horarioFuncionamento()));

        if (d.capacidadeAlunos() != null && d.capacidadeAlunos() < 0) {
            throw new NegocioException("A capacidade de alunos não pode ser negativa.");
        }
        e.setCapacidadeAlunos(d.capacidadeAlunos());

        String cep = Texto.soNumeros(d.cep());
        if (!cep.isEmpty() && cep.length() != 8) {
            throw new NegocioException("O CEP deve ter 8 números.");
        }
        e.setCep(cep.isEmpty() ? null : cep);
        e.setLogradouro(Texto.limpo(d.logradouro()));
        e.setNumero(Texto.limpo(d.numero()));
        e.setComplemento(Texto.limpo(d.complemento()));
        e.setBairro(Texto.limpo(d.bairro()));
        e.setCidade(Texto.limpo(d.cidade()));

        String uf = Texto.limpo(d.uf());
        if (uf != null) {
            uf = uf.toUpperCase();
            if (uf.length() != 2) {
                throw new NegocioException("A UF deve ter 2 letras (ex: GO).");
            }
        }
        e.setUf(uf);
        e.setObservacoes(Texto.limpo(d.observacoes()));
    }
}
