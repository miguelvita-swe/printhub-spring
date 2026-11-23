package br.edu.unifio.printhub.entidades;

/**
 * Ciclo de vida do lote coletivo de entrega.
 *
 * EM_FORMACAO -> FECHADO (3 pedidos ou fechamento manual) -> ENTREGUE_NO_PONTO -> CONCLUIDO
 */
public enum StatusLote {
    EM_FORMACAO,
    FECHADO,
    ENTREGUE_NO_PONTO,
    CONCLUIDO
}
