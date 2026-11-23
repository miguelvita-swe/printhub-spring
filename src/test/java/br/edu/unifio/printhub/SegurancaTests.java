package br.edu.unifio.printhub;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/** Diferença entre 401 (não autenticado) e 403 (autenticado sem permissão). */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SegurancaTests {

    @Autowired
    MockMvc mvc;

    @Test
    void semLoginDevolve401() throws Exception {
        mvc.perform(get("/produtos"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Basic realm=\"PrintHub\""))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void senhaErradaDevolve401() throws Exception {
        mvc.perform(get("/produtos").with(httpBasic("admin", "senha-errada")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void qualquerPerfilLogadoConsulta() throws Exception {
        mvc.perform(get("/produtos").with(httpBasic("consumidor", "consumidor123"))).andExpect(status().isOk());
        mvc.perform(get("/categorias").with(httpBasic("produtor", "produtor123"))).andExpect(status().isOk());
    }

    @Test
    void consumidorNaoCriaCategoria403() throws Exception {
        mvc.perform(post("/categorias").with(httpBasic("consumidor", "consumidor123"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"nome\": \"Teste\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.mensagem").value(
                        "O perfil do usuário consumidor não tem permissão para POST /categorias"));
    }

    @Test
    void soAdminConsultaUsuarios() throws Exception {
        mvc.perform(get("/usuarios").with(httpBasic("consumidor", "consumidor123")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/usuarios").with(httpBasic("admin", "admin123"))).andExpect(status().isOk());
    }

    @Test
    void consumidorNaoAceitaPedido403() throws Exception {
        mvc.perform(post("/pedidos/1/aceitar").with(httpBasic("consumidor", "consumidor123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void produtorNaoFazPedido403() throws Exception {
        mvc.perform(post("/pedidos").with(httpBasic("produtor", "produtor123"))
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }
}
