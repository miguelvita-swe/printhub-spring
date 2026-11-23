package br.edu.unifio.printhub.excecoes;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Formato único de todas as respostas de erro da API.
 *
 * Exemplo:
 * { "momento": "...", "status": 409, "erro": "Conflict",
 *   "mensagem": "Estoque insuficiente para Vaso geométrico: pedido 5, disponível 2",
 *   "caminho": "/pedidos" }
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ErroResposta(
        LocalDateTime momento,
        int status,
        String erro,
        String mensagem,
        String caminho,
        List<CampoInvalido> campos) {

    /** Campo do corpo da requisição que não passou na validação. */
    public record CampoInvalido(String campo, String mensagem) {
    }
}
