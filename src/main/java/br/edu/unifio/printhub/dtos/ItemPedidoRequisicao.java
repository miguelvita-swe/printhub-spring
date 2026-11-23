package br.edu.unifio.printhub.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Um produto e a quantidade desejada, dentro de um pedido. */
public record ItemPedidoRequisicao(
        @NotNull(message = "O produto é obrigatório")
        Integer produtoId,

        @NotNull(message = "A quantidade é obrigatória")
        @Min(value = 1, message = "A quantidade mínima é 1")
        @Max(value = 50, message = "A quantidade máxima por item é 50")
        Integer quantidade) {
}
