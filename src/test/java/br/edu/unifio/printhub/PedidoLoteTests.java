package br.edu.unifio.printhub;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

/** Fluxo do pedido e funcionalidade específica do PrintHub: o lote coletivo. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PedidoLoteTests {

    static final RequestPostProcessor ADMIN = httpBasic("admin", "admin123");
    static final RequestPostProcessor PRODUTOR = httpBasic("produtor", "produtor123");
    static final RequestPostProcessor CONSUMIDOR = httpBasic("consumidor", "consumidor123");

    // Dados do import.sql: usuários 1 e 2 são produtores, 3, 4 e 5 consumidores.
    // Produtos 1, 2 e 3 são do produtor 1; 4, 5 e 6 do produtor 2. Ponto 1 custa R$ 20,00.

    @Autowired
    MockMvc mvc;

    private ResultActions criarPedido(int consumidorId, int pontoId, String itens) throws Exception {
        return mvc.perform(post("/pedidos").with(CONSUMIDOR).contentType(MediaType.APPLICATION_JSON)
                .content("{\"consumidorId\": " + consumidorId + ", \"pontoRetiradaId\": " + pontoId
                        + ", \"itens\": [" + itens + "]}"));
    }

    private int idDe(ResultActions resultado) throws Exception {
        return JsonPath.read(resultado.andReturn().getResponse().getContentAsString(), "$.id");
    }

    private int pedidoAceito(int consumidorId, String itens) throws Exception {
        int id = idDe(criarPedido(consumidorId, 1, itens).andExpect(status().isCreated()));
        mvc.perform(post("/pedidos/" + id + "/aceitar").with(PRODUTOR)).andExpect(status().isOk());
        return id;
    }

    @Test
    void loteFechaNoTerceiroPedidoEDivideAEntrega() throws Exception {
        ResultActions criado = criarPedido(3, 1, "{\"produtoId\": 1, \"quantidade\": 2}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("AGUARDANDO_ACEITACAO"))
                .andExpect(jsonPath("$.valorProdutos").value(96.00))
                .andExpect(jsonPath("$.taxaEntrega").value(nullValue()));
        int primeiro = idDe(criado);

        String aceito = mvc.perform(post("/pedidos/" + primeiro + "/aceitar").with(PRODUTOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACEITO"))
                .andExpect(jsonPath("$.posicaoLote").value(1))
                .andExpect(jsonPath("$.loteStatus").value("EM_FORMACAO"))
                .andReturn().getResponse().getContentAsString();
        int loteId = JsonPath.read(aceito, "$.loteId");

        mvc.perform(get("/produtos/1").with(ADMIN)).andExpect(jsonPath("$.estoque").value(8));

        pedidoAceito(4, "{\"produtoId\": 2, \"quantidade\": 1}");
        mvc.perform(get("/lotes/" + loteId).with(ADMIN))
                .andExpect(jsonPath("$.vagasRestantes").value(1));

        pedidoAceito(5, "{\"produtoId\": 3, \"quantidade\": 1}");

        mvc.perform(get("/lotes/" + loteId).with(ADMIN))
                .andExpect(jsonPath("$.status").value("FECHADO"))
                .andExpect(jsonPath("$.codigo").value(String.format("LOTE-%03d", loteId)))
                .andExpect(jsonPath("$.pedidos[*].taxaEntrega", contains(6.67, 6.67, 6.66)));

        mvc.perform(get("/pedidos/" + primeiro).with(CONSUMIDOR))
                .andExpect(jsonPath("$.taxaEntrega").value(6.67))
                .andExpect(jsonPath("$.valorTotal").value(102.67));

        // Lote fechado: não dá mais para cancelar.
        mvc.perform(post("/pedidos/" + primeiro + "/cancelar").with(CONSUMIDOR))
                .andExpect(status().isConflict());

        // Retirar antes de o lote chegar ao ponto é proibido.
        mvc.perform(post("/pedidos/" + primeiro + "/retirar").with(ADMIN)).andExpect(status().isConflict());

        mvc.perform(post("/lotes/" + loteId + "/receber").with(ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENTREGUE_NO_PONTO"))
                .andExpect(jsonPath("$.pedidos[*].status",
                        contains("DISPONIVEL_RETIRADA", "DISPONIVEL_RETIRADA", "DISPONIVEL_RETIRADA")));

        String lote = mvc.perform(get("/lotes/" + loteId).with(ADMIN)).andReturn().getResponse()
                .getContentAsString();
        for (int i = 0; i < 3; i++) {
            int pedidoId = JsonPath.read(lote, "$.pedidos[" + i + "].pedidoId");
            mvc.perform(post("/pedidos/" + pedidoId + "/retirar").with(ADMIN))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("RETIRADO"));
        }
        mvc.perform(get("/lotes/" + loteId).with(ADMIN)).andExpect(jsonPath("$.status").value("CONCLUIDO"));
    }

    @Test
    void estoqueInsuficienteDevolve409() throws Exception {
        criarPedido(3, 1, "{\"produtoId\": 4, \"quantidade\": 3}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value(
                        "Estoque insuficiente para Miniatura de dragão: pedido 3, disponível 2"));
    }

    @Test
    void segundoAceiteSemEstoqueDevolve409ENaoDeixaEstoqueNegativo() throws Exception {
        int a = idDe(criarPedido(3, 1, "{\"produtoId\": 4, \"quantidade\": 2}"));
        int b = idDe(criarPedido(4, 1, "{\"produtoId\": 4, \"quantidade\": 2}"));
        mvc.perform(post("/pedidos/" + a + "/aceitar").with(PRODUTOR)).andExpect(status().isOk());
        mvc.perform(post("/pedidos/" + b + "/aceitar").with(PRODUTOR))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value(
                        "Estoque insuficiente para Miniatura de dragão: pedido 2, disponível 0"));
        mvc.perform(get("/produtos/4").with(ADMIN)).andExpect(jsonPath("$.estoque").value(0));
        mvc.perform(get("/pedidos/" + b).with(ADMIN)).andExpect(jsonPath("$.status").value("AGUARDANDO_ACEITACAO"));
    }

    @Test
    void produtorNaoCompraOProprioProduto() throws Exception {
        criarPedido(1, 1, "{\"produtoId\": 1, \"quantidade\": 1}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value(
                        "O usuário Carlos Mendes é o produtor de Vaso geométrico e não pode comprar o próprio produto"));
    }

    @Test
    void itensDeProdutoresDiferentesDevolve409() throws Exception {
        criarPedido(3, 1, "{\"produtoId\": 1, \"quantidade\": 1}, {\"produtoId\": 5, \"quantidade\": 1}")
                .andExpect(status().isConflict());
    }

    @Test
    void pedidoSemItensOuComQuantidadeZeroDevolve400() throws Exception {
        criarPedido(3, 1, "").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo").value("itens"));
        criarPedido(3, 1, "{\"produtoId\": 1, \"quantidade\": 0}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo").value("itens[0].quantidade"));
    }

    @Test
    void pedidoParaProdutoOuPontoInexistenteDevolve404() throws Exception {
        criarPedido(3, 99, "{\"produtoId\": 1, \"quantidade\": 1}").andExpect(status().isNotFound());
        criarPedido(3, 1, "{\"produtoId\": 99, \"quantidade\": 1}").andExpect(status().isNotFound());
    }

    @Test
    void cancelarPedidoAceitoDevolveEstoqueEReorganizaOLote() throws Exception {
        int primeiro = pedidoAceito(3, "{\"produtoId\": 1, \"quantidade\": 2}");
        int segundo = pedidoAceito(4, "{\"produtoId\": 2, \"quantidade\": 1}");

        mvc.perform(post("/pedidos/" + primeiro + "/cancelar").with(CONSUMIDOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"))
                .andExpect(jsonPath("$.loteId").value(nullValue()));
        mvc.perform(get("/produtos/1").with(ADMIN)).andExpect(jsonPath("$.estoque").value(10));
        mvc.perform(get("/pedidos/" + segundo).with(ADMIN)).andExpect(jsonPath("$.posicaoLote").value(1));
    }

    @Test
    void fechamentoManualComDoisPedidos() throws Exception {
        int id = pedidoAceito(3, "{\"produtoId\": 1, \"quantidade\": 1}");
        pedidoAceito(4, "{\"produtoId\": 2, \"quantidade\": 1}");
        String pedido = mvc.perform(get("/pedidos/" + id).with(ADMIN)).andReturn().getResponse()
                .getContentAsString();
        int loteId = JsonPath.read(pedido, "$.loteId");

        mvc.perform(post("/lotes/" + loteId + "/fechar").with(ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FECHADO"))
                .andExpect(jsonPath("$.pedidos[*].taxaEntrega", contains(10.00, 10.00)));
        mvc.perform(post("/lotes/" + loteId + "/fechar").with(ADMIN)).andExpect(status().isConflict());
        mvc.perform(delete("/lotes/" + loteId).with(ADMIN)).andExpect(status().isConflict());
    }

    @Test
    void crudDeLote() throws Exception {
        String criado = mvc.perform(post("/lotes").with(ADMIN).contentType(MediaType.APPLICATION_JSON)
                .content("{\"pontoRetiradaId\": 2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("EM_FORMACAO"))
                .andExpect(jsonPath("$.custoEntrega").value(15.00))
                .andReturn().getResponse().getContentAsString();
        int loteId = JsonPath.read(criado, "$.id");

        // Um ponto só pode ter um lote em formação.
        mvc.perform(post("/lotes").with(ADMIN).contentType(MediaType.APPLICATION_JSON)
                .content("{\"pontoRetiradaId\": 2}")).andExpect(status().isConflict());

        mvc.perform(put("/lotes/" + loteId).with(ADMIN).contentType(MediaType.APPLICATION_JSON)
                .content("{\"prazo\": \"2099-12-31T18:00:00\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prazo").value("2099-12-31T18:00:00"));
        mvc.perform(put("/lotes/" + loteId).with(ADMIN).contentType(MediaType.APPLICATION_JSON)
                .content("{\"prazo\": \"2000-01-01T00:00:00\"}")).andExpect(status().isBadRequest());

        mvc.perform(get("/lotes/ponto/2?status=EM_FORMACAO").with(CONSUMIDOR)).andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(delete("/lotes/" + loteId).with(ADMIN)).andExpect(status().isNoContent());
    }

    @Test
    void crudDeItensDoPedido() throws Exception {
        int pedido = idDe(criarPedido(3, 1, "{\"produtoId\": 1, \"quantidade\": 1}"));

        String comDois = mvc.perform(post("/pedidos/" + pedido + "/itens").with(CONSUMIDOR)
                .contentType(MediaType.APPLICATION_JSON).content("{\"produtoId\": 2, \"quantidade\": 2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.itens", hasSize(2)))
                .andExpect(jsonPath("$.valorProdutos").value(98.00))
                .andReturn().getResponse().getContentAsString();
        int itemNovo = JsonPath.read(comDois, "$.itens[1].id");

        mvc.perform(get("/pedidos/" + pedido + "/itens/" + itemNovo).with(CONSUMIDOR))
                .andExpect(jsonPath("$.produtoNome").value("Suporte para celular"));

        mvc.perform(put("/pedidos/" + pedido + "/itens/" + itemNovo).with(CONSUMIDOR)
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantidade\": 3}"))
                .andExpect(jsonPath("$.valorProdutos").value(123.00));

        // Produto de outro produtor não entra no mesmo pedido.
        mvc.perform(post("/pedidos/" + pedido + "/itens").with(CONSUMIDOR)
                .contentType(MediaType.APPLICATION_JSON).content("{\"produtoId\": 5, \"quantidade\": 1}"))
                .andExpect(status().isConflict());

        mvc.perform(delete("/pedidos/" + pedido + "/itens/" + itemNovo).with(CONSUMIDOR))
                .andExpect(jsonPath("$.itens", hasSize(1)));

        int ultimo = JsonPath.read(mvc.perform(get("/pedidos/" + pedido).with(CONSUMIDOR))
                .andReturn().getResponse().getContentAsString(), "$.itens[0].id");
        mvc.perform(delete("/pedidos/" + pedido + "/itens/" + ultimo).with(CONSUMIDOR))
                .andExpect(status().isConflict());
        mvc.perform(get("/pedidos/" + pedido + "/itens/999").with(CONSUMIDOR)).andExpect(status().isNotFound());
    }

    @Test
    void atualizarEExcluirPedido() throws Exception {
        int pedido = idDe(criarPedido(3, 1, "{\"produtoId\": 1, \"quantidade\": 1}"));
        mvc.perform(put("/pedidos/" + pedido).with(CONSUMIDOR).contentType(MediaType.APPLICATION_JSON)
                .content("{\"consumidorId\": 3, \"pontoRetiradaId\": 2,"
                        + " \"itens\": [{\"produtoId\": 1, \"quantidade\": 3}, {\"produtoId\": 3, \"quantidade\": 1}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pontoRetiradaNome").value("Ponto Vila Margarida"))
                .andExpect(jsonPath("$.itens", hasSize(2)))
                .andExpect(jsonPath("$.valorProdutos").value(183.90));
        mvc.perform(delete("/pedidos/" + pedido).with(CONSUMIDOR)).andExpect(status().isNoContent());
        mvc.perform(get("/pedidos/" + pedido).with(CONSUMIDOR)).andExpect(status().isNotFound());
    }

    @Test
    void rejeitarExigeMotivoEBloqueiaAceite() throws Exception {
        int pedido = idDe(criarPedido(3, 1, "{\"produtoId\": 1, \"quantidade\": 1}"));
        mvc.perform(post("/pedidos/" + pedido + "/rejeitar").with(PRODUTOR)
                .contentType(MediaType.APPLICATION_JSON).content("{\"motivo\": \"\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/pedidos/" + pedido + "/rejeitar").with(PRODUTOR)
                .contentType(MediaType.APPLICATION_JSON).content("{\"motivo\": \"Impressora em manutenção\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJEITADO"));
        mvc.perform(post("/pedidos/" + pedido + "/aceitar").with(PRODUTOR)).andExpect(status().isConflict());
    }

    @Test
    void consultasPersonalizadasDePedido() throws Exception {
        pedidoAceito(3, "{\"produtoId\": 1, \"quantidade\": 1}");
        idDe(criarPedido(3, 1, "{\"produtoId\": 2, \"quantidade\": 1}"));
        mvc.perform(get("/pedidos/status/ACEITO").with(ADMIN)).andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get("/pedidos/consumidor/3").with(ADMIN)).andExpect(jsonPath("$", hasSize(2)));
        mvc.perform(get("/pedidos/consumidor/3?status=AGUARDANDO_ACEITACAO").with(ADMIN))
                .andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get("/lotes/ponto/1?status=EM_FORMACAO").with(ADMIN)).andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get("/pedidos/status/INVALIDO").with(ADMIN)).andExpect(status().isBadRequest());
    }
}
