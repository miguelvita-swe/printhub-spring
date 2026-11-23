package br.edu.unifio.printhub.dtos;

import java.math.BigDecimal;

import br.edu.unifio.printhub.entidades.ItemPedido;

public record ItemPedidoResposta(
        Integer id,
        Integer produtoId,
        String produtoNome,
        Integer quantidade,
        BigDecimal precoUnitario,
        BigDecimal subtotal) {

    public static ItemPedidoResposta de(ItemPedido item) {
        return new ItemPedidoResposta(item.getId(), item.getProduto().getId(), item.getProduto().getNome(),
                item.getQuantidade(), item.getPrecoUnitario(), item.getSubtotal());
    }
}
