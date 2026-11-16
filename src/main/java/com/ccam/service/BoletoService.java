package com.ccam.service;

import com.ccam.exception.NegocioException;
import com.ccam.model.Cliente;
import com.ccam.model.Financeiro;
import com.ccam.repository.ClienteRepository;
import com.ccam.repository.FinanceiroRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class BoletoService {
    private static final Logger log = LoggerFactory.getLogger(BoletoService.class);
    private static final Set<String> PAGOS = Set.of("RECEIVED", "CONFIRMED", "RECEIVED_IN_CASH", "DUNNING_RECEIVED");

    private final String chave;
    private final RestClient http;
    private final FinanceiroRepository financeiro;
    private final ClienteRepository clientes;
    private final LogService logService;

    public BoletoService(@Value("${ccam.asaas.url}") String url, @Value("${ccam.asaas.chave:}") String chave,
                         FinanceiroRepository financeiro, ClienteRepository clientes, LogService logService) {
        this.chave = chave == null ? "" : chave.trim();
        this.http = RestClient.builder().baseUrl(url).build();
        this.financeiro = financeiro;
        this.clientes = clientes;
        this.logService = logService;
    }

    public boolean disponivel() {
        return !chave.isEmpty();
    }

    public Financeiro gerar(Cliente aluno, Financeiro f) {
        if (!disponivel()) {
            throw new NegocioException("A academia ainda não ativou o boleto. Pague por Pix ou na recepção.");
        }
        if (f.getBoletoId() != null && f.getBoletoUrl() != null) {
            return f;
        }
        try {
            String clienteAsaas = clienteAsaas(aluno);
            LocalDate hoje = LocalDate.now();
            Map<String, Object> corpo = new HashMap<>();
            corpo.put("customer", clienteAsaas);
            corpo.put("billingType", "BOLETO");
            corpo.put("value", f.getValor());
            corpo.put("dueDate", (f.getVencimento() == null || f.getVencimento().isBefore(hoje) ? hoje.plusDays(1) : f.getVencimento()).toString());
            corpo.put("description", "Mensalidade da academia");
            corpo.put("externalReference", f.getId().toString());
            Map<?, ?> cobranca = enviar("POST", "/payments", corpo);
            f.setBoletoId(texto(cobranca.get("id")));
            f.setBoletoUrl(texto(cobranca.get("bankSlipUrl")));
            try {
                Map<?, ?> linha = enviar("GET", "/payments/" + f.getBoletoId() + "/identificationField", null);
                f.setLinhaDigitavel(texto(linha.get("identificationField")));
            } catch (NegocioException e) {
                log.warn("Linha digitável indisponível para {}: {}", f.getBoletoId(), e.getMessage());
            }
            return financeiro.save(f);
        } catch (RestClientException e) {
            log.warn("Falha ao gerar boleto: {}", e.getMessage());
            throw new NegocioException("Não foi possível gerar o boleto agora. Tente de novo em alguns minutos.");
        }
    }

    private String clienteAsaas(Cliente aluno) {
        if (aluno.getAsaasClienteId() != null) {
            return aluno.getAsaasClienteId();
        }
        Map<String, Object> corpo = new HashMap<>();
        corpo.put("name", aluno.getNome());
        corpo.put("cpfCnpj", aluno.getCpf() == null ? null : aluno.getCpf().replaceAll("[^0-9]", ""));
        corpo.put("email", aluno.getEmail());
        corpo.put("externalReference", aluno.getId().toString());
        corpo.put("notificationDisabled", true);
        Map<?, ?> criado = enviar("POST", "/customers", corpo);
        aluno.setAsaasClienteId(texto(criado.get("id")));
        clientes.save(aluno);
        return aluno.getAsaasClienteId();
    }

    private Map<?, ?> enviar(String metodo, String caminho, Object corpo) {
        try {
            var req = "POST".equals(metodo) ? http.post().uri(caminho).contentType(MediaType.APPLICATION_JSON).body(corpo) : http.get().uri(caminho);
            Map<?, ?> resp = req.header("access_token", chave).header("User-Agent", "ccam").retrieve().body(Map.class);
            if (resp == null) {
                throw new NegocioException("Resposta vazia do banco.");
            }
            return resp;
        } catch (RestClientResponseException e) {
            log.warn("Asaas respondeu {} em {}: {}", e.getStatusCode(), caminho, e.getResponseBodyAsString());
            throw new NegocioException("O banco recusou o pedido do boleto. Confira os dados do aluno (CPF e e-mail).");
        }
    }

    @Scheduled(fixedDelay = 20 * 60 * 1000, initialDelay = 60 * 1000)
    public void verificarPagamentos() {
        if (!disponivel()) {
            return;
        }
        List<Financeiro> abertos = financeiro.findByBoletoIdIsNotNullAndStatusAndPagamentoInformadoEmIsNull("Pendente");
        for (Financeiro f : abertos) {
            try {
                Map<?, ?> cobranca = enviar("GET", "/payments/" + f.getBoletoId(), null);
                if (PAGOS.contains(texto(cobranca.get("status")))) {
                    f.setPagamentoInformadoEm(LocalDateTime.now());
                    financeiro.save(f);
                    logService.registrarPeloSistema(f.getEmpresaId(), LogService.FINANCEIRO, f.getId(), f.getClienteId(), LogService.EDICAO,
                            "Boleto pago, confirmado pelo banco. Aguardando a confirmação da academia.");
                }
            } catch (RuntimeException e) {
                log.warn("Não foi possível consultar o boleto {}: {}", f.getBoletoId(), e.getMessage());
            }
        }
    }

    private static String texto(Object v) {
        return v == null ? null : v.toString();
    }
}
