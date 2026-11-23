package br.edu.unifio.printhub.entidades;

/**
 * Ciclo de vida do pedido.
 *
 * AGUARDANDO_ACEITACAO -> ACEITO (entra no lote) -> DISPONIVEL_RETIRADA -> RETIRADO
 *                      -> REJEITADO (produtor recusou)
 *                      -> CANCELADO (consumidor desistiu)
 */
public enum StatusPedido {
    AGUARDANDO_ACEITACAO,
    ACEITO,
    REJEITADO,
    CANCELADO,
    DISPONIVEL_RETIRADA,
    RETIRADO
}
