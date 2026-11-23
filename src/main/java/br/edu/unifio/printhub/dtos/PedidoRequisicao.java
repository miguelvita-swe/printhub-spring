package br.edu.unifio.printhub.dtos;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/** Dados para fazer (ou refazer, enquanto não foi aceito) um pedido. */
public record PedidoRequisicao(
        @NotNull(message = "O consumidor é obrigatório")
        Integer consumidorId,

        @NotNull(message = "O ponto de retirada é obrigatório")
        Integer pontoRetiradaId,

        @NotEmpty(message = "O pedido precisa de pelo menos um item")
        @Valid
        List<ItemPedidoRequisicao> itens) {
}
