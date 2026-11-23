package br.edu.unifio.printhub.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import br.edu.unifio.printhub.entidades.Lote;
import br.edu.unifio.printhub.entidades.Pedido;
import br.edu.unifio.printhub.entidades.StatusLote;
import br.edu.unifio.printhub.entidades.StatusPedido;

public record LoteResposta(
        Integer id,
        /** Código amigável, ex.: LOTE-001. */
        String codigo,
        StatusLote status,
        Integer pontoRetiradaId,
        String pontoRetiradaNome,
        BigDecimal custoEntrega,
        int quantidadePedidos,
        int vagasRestantes,
        LocalDateTime abertoEm,
        LocalDateTime prazo,
        LocalDateTime fechadoEm,
        List<PedidoNoLote> pedidos) {

    /** Resumo de cada pedido do lote, com a parte da entrega que cabe a ele. */
    public record PedidoNoLote(Integer pedidoId, Integer posicao, String consumidorNome, StatusPedido status,
            BigDecimal taxaEntrega) {

        static PedidoNoLote de(Pedido pedido) {
            return new PedidoNoLote(pedido.getId(), pedido.getPosicaoLote(), pedido.getConsumidor().getNome(),
                    pedido.getStatus(), pedido.getTaxaEntrega());
        }
    }

    public static LoteResposta de(Lote lote) {
        int quantidade = lote.getPedidos().size();
        int vagas = lote.getStatus() == StatusLote.EM_FORMACAO ? Lote.LIMITE_PEDIDOS - quantidade : 0;
        return new LoteResposta(
                lote.getId(),
                String.format("LOTE-%03d", lote.getId()),
                lote.getStatus(),
                lote.getPontoRetirada().getId(),
                lote.getPontoRetirada().getNome(),
                lote.getCustoEntrega(),
                quantidade,
                vagas,
                lote.getAbertoEm(),
                lote.getPrazo(),
                lote.getFechadoEm(),
                lote.getPedidos().stream().map(PedidoNoLote::de).toList());
    }
}
