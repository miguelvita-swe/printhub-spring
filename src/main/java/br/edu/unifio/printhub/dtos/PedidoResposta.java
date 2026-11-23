package br.edu.unifio.printhub.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import br.edu.unifio.printhub.entidades.Pedido;
import br.edu.unifio.printhub.entidades.StatusLote;
import br.edu.unifio.printhub.entidades.StatusPedido;

public record PedidoResposta(
        Integer id,
        StatusPedido status,
        Integer consumidorId,
        String consumidorNome,
        Integer produtorId,
        String produtorNome,
        Integer pontoRetiradaId,
        String pontoRetiradaNome,
        Integer loteId,
        StatusLote loteStatus,
        Integer posicaoLote,
        List<ItemPedidoResposta> itens,
        BigDecimal valorProdutos,
        /** Nula até o lote fechar. */
        BigDecimal taxaEntrega,
        BigDecimal valorTotal,
        String motivoRejeicao,
        LocalDateTime criadoEm,
        LocalDateTime aceitoEm,
        LocalDateTime retiradoEm) {

    public static PedidoResposta de(Pedido pedido) {
        return new PedidoResposta(
                pedido.getId(),
                pedido.getStatus(),
                pedido.getConsumidor().getId(),
                pedido.getConsumidor().getNome(),
                pedido.getProdutor().getId(),
                pedido.getProdutor().getNome(),
                pedido.getPontoRetirada().getId(),
                pedido.getPontoRetirada().getNome(),
                pedido.getLote() == null ? null : pedido.getLote().getId(),
                pedido.getLote() == null ? null : pedido.getLote().getStatus(),
                pedido.getPosicaoLote(),
                pedido.getItens().stream().map(ItemPedidoResposta::de).toList(),
                pedido.getValorProdutos(),
                pedido.getTaxaEntrega(),
                pedido.getValorTotal(),
                pedido.getMotivoRejeicao(),
                pedido.getCriadoEm(),
                pedido.getAceitoEm(),
                pedido.getRetiradoEm());
    }
}
