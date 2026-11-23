package br.edu.unifio.printhub;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

/** CRUD, validações e consultas de categorias, usuários, pontos e produtos. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CatalogoTests {

    static final RequestPostProcessor ADMIN = httpBasic("admin", "admin123");
    static final RequestPostProcessor PRODUTOR = httpBasic("produtor", "produtor123");

    @Autowired
    MockMvc mvc;

    @Test
    void criaAtualizaEExcluiCategoria() throws Exception {
        String corpo = mvc.perform(post("/categorias").with(ADMIN).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\": \"Cosplay\", \"descricao\": \"Acessórios para fantasias\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.nome").value("Cosplay"))
                .andReturn().getResponse().getContentAsString();
        Integer id = Integer.valueOf(corpo.replaceAll(".*\"id\":(\\d+).*", "$1"));

        mvc.perform(put("/categorias/" + id).with(ADMIN).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\": \"Cosplay e fantasias\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Cosplay e fantasias"));

        mvc.perform(delete("/categorias/" + id).with(ADMIN)).andExpect(status().isNoContent());
        mvc.perform(get("/categorias/" + id).with(ADMIN)).andExpect(status().isNotFound());
    }

    @Test
    void naoExcluiCategoriaComProdutos() throws Exception {
        mvc.perform(delete("/categorias/1").with(ADMIN))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value(
                        "A categoria 'Decoração' tem produtos associados e não pode ser excluída"));
    }

    @Test
    void naoCriaCategoriaComNomeRepetido() throws Exception {
        mvc.perform(post("/categorias").with(ADMIN).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\": \"utilidades\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void recursoInexistenteDevolve404() throws Exception {
        mvc.perform(get("/produtos/999").with(ADMIN))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value("Produto com id 999 não foi encontrado"));
    }

    @Test
    void produtoComDadosInvalidosDevolve400ComOsCampos() throws Exception {
        mvc.perform(post("/produtos").with(PRODUTOR).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\": \"\", \"material\": \"PLA\", \"preco\": 0, \"estoque\": -1,"
                        + " \"diasProducao\": 3, \"categoriaId\": 1, \"produtorId\": 1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos", hasSize(3)))
                .andExpect(jsonPath("$.campos[*].campo", hasItem("preco")))
                .andExpect(jsonPath("$.campos[*].mensagem", hasItem("O preço deve ser maior que zero")));
    }

    @Test
    void jsonMalFormadoEIdNaoNumericoDevolvem400() throws Exception {
        mvc.perform(post("/categorias").with(ADMIN).contentType(MediaType.APPLICATION_JSON).content("{\"nome\": "))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/produtos/abc").with(ADMIN))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value(
                        "O parâmetro 'id' recebeu 'abc', mas espera um valor do tipo Integer"));
        mvc.perform(get("/usuarios/tipo/VENDEDOR").with(ADMIN)).andExpect(status().isBadRequest());
    }

    @Test
    void rotaInexistenteDevolve404() throws Exception {
        mvc.perform(get("/nao-existe").with(ADMIN)).andExpect(status().isNotFound());
    }

    @Test
    void consumidorNaoPodeAnunciarProduto() throws Exception {
        mvc.perform(post("/produtos").with(PRODUTOR).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\": \"Chaveiro\", \"material\": \"PLA\", \"preco\": 8.50, \"estoque\": 20,"
                        + " \"diasProducao\": 1, \"categoriaId\": 2, \"produtorId\": 3}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value(
                        "O usuário João Pereira é CONSUMIDOR e só produtores podem anunciar produtos"));
    }

    @Test
    void criaEAtualizaProduto() throws Exception {
        String corpo = mvc.perform(post("/produtos").with(PRODUTOR).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\": \"Chaveiro personalizado\", \"material\": \"PLA\", \"cor\": \"Azul\","
                        + " \"preco\": 8.50, \"estoque\": 20, \"diasProducao\": 1, \"categoriaId\": 2,"
                        + " \"produtorId\": 1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoriaNome").value("Utilidades"))
                .andExpect(jsonPath("$.produtorNome").value("Carlos Mendes"))
                .andReturn().getResponse().getContentAsString();
        Integer id = Integer.valueOf(corpo.replaceAll(".*\"id\":(\\d+).*", "$1"));

        mvc.perform(put("/produtos/" + id).with(PRODUTOR).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\": \"Chaveiro personalizado\", \"material\": \"PETG\", \"preco\": 9.90,"
                        + " \"estoque\": 18, \"diasProducao\": 1, \"categoriaId\": 2, \"produtorId\": 1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preco").value(9.90))
                .andExpect(jsonPath("$.material").value("PETG"));
    }

    @Test
    void consultasPersonalizadasDeProduto() throws Exception {
        mvc.perform(get("/produtos/categoria/2").with(ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nome").value(
                        org.hamcrest.Matchers.contains("Organizador de mesa", "Suporte para celular")));
        mvc.perform(get("/produtos/estoque-baixo?limite=5").with(ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nome").value("Miniatura de dragão"));
        mvc.perform(get("/produtos/estoque-baixo?limite=0").with(ADMIN))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/produtos/busca?nome=VASO").with(ADMIN))
                .andExpect(jsonPath("$[0].nome").value("Vaso geométrico"));
        mvc.perform(get("/usuarios/tipo/PRODUTOR").with(ADMIN))
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void usuarioComEmailRepetidoDevolve409() throws Exception {
        mvc.perform(post("/usuarios").with(ADMIN).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\": \"Outro João\", \"email\": \"JOAO@printhub.local\", \"tipo\": \"CONSUMIDOR\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void crudDePontoDeRetirada() throws Exception {
        String corpo = mvc.perform(post("/pontos-retirada").with(ADMIN).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\": \"Ponto Universidade\", \"endereco\": \"Rua Teste, 1\", \"bairro\": \"Centro\","
                        + " \"cidade\": \"Ourinhos\", \"custoEntrega\": 10.00}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ativo").value(true))
                .andReturn().getResponse().getContentAsString();
        Integer id = Integer.valueOf(corpo.replaceAll(".*\"id\":(\\d+).*", "$1"));
        mvc.perform(delete("/pontos-retirada/" + id).with(ADMIN)).andExpect(status().isNoContent());
    }
}
