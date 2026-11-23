package br.edu.unifio.printhub.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Nova quantidade de um item já existente no pedido. */
public record QuantidadeRequisicao(
        @NotNull(message = "A quantidade é obrigatória")
        @Min(value = 1, message = "A quantidade mínima é 1")
        @Max(value = 50, message = "A quantidade máxima por item é 50")
        Integer quantidade) {
}
